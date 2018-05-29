package com.generallycloud.baseio.component;

import java.io.IOException;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.CompletionHandler;

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.ReleaseUtil;
import com.generallycloud.baseio.component.ssl.SslHandler;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.ChannelFuture;
import com.generallycloud.baseio.protocol.ProtocolCodec;
import com.generallycloud.baseio.protocol.SslFuture;

public class ReadCompletionHandler implements CompletionHandler<Integer, AioSocketChannel> {

    private Logger                   logger = LoggerFactory.getLogger(getClass());

    private final ForeFutureAcceptor foreFutureAcceptor;

    public ReadCompletionHandler(ForeFutureAcceptor foreFutureAcceptor) {
        this.foreFutureAcceptor = foreFutureAcceptor;
    }

    @Override
    public void completed(Integer result, AioSocketChannel ch) {
        CachedAioThread context = ch.getChannelThreadContext();
        SslFuture sslTemporary = context.getSslTemporary();
        try {
            ByteBuf buf = ch.getReadCache();
            if (result < 1) {
                if (result == 0) {
                    if (!ch.isEnableSsl()) {
                        ByteBuf remainingBuf = ch.getRemainingBuf();
                        if (remainingBuf != null) {
                            buf.read(remainingBuf);
                        }
                    }
                    ch.read(buf);
                    return;
                }
                CloseUtil.close(ch);
                return;
            }
            ch.active();
            buf.reverse();
            buf.flip();
            buf.reverse();
            buf.flip();
            ch.active();
            if (ch.isEnableSsl()) {
                for (;;) {
                    if (!buf.hasRemaining()) {
                        return;
                    }
                    SslFuture future = ch.getSslReadFuture();
                    boolean setFutureNull = true;
                    if (future == null) {
                        future = sslTemporary.reset();
                        setFutureNull = false;
                    }
                    if (!future.read(ch, buf)) {
                        if (!setFutureNull) {
                            if (future == sslTemporary) {
                                future = future.copy(ch);
                            }
                            ch.setSslReadFuture(future);
                        }
                        return;
                    }
                    if (setFutureNull) {
                        ch.setSslReadFuture(null);
                    }
                    SslHandler sslHandler = ch.getSslHandler();
                    ByteBuf product;
                    try {
                        product = sslHandler.unwrap(ch, future.getByteBuf());
                    } finally {
                        ReleaseUtil.release(future, ch.getChannelThreadContext());
                    }
                    if (product == null) {
                        continue;
                    }
                    accept(ch, product);
                }
            } else {
                accept(ch, buf);
            }
        } catch (Exception e) {
            failed(e, ch);
        }
        ch.read(ch.getReadCache());
    }

    private void accept(SocketChannel ch, ByteBuf buffer) throws Exception {
        ProtocolCodec codec = ch.getProtocolCodec();
        ByteBufAllocator allocator = ch.allocator();
        for (;;) {
            if (!buffer.hasRemaining()) {
                return;
            }
            ChannelFuture future = ch.getReadFuture();
            boolean setFutureNull = true;
            if (future == null) {
                future = codec.decode(ch, buffer);
                setFutureNull = false;
            }
            try {
                if (!future.read(ch, buffer)) {
                    if (!setFutureNull) {
                        ch.setReadFuture(future);
                    }
                    ByteBuf remainingBuf = ch.getRemainingBuf();
                    if (remainingBuf != null) {
                        remainingBuf.release(remainingBuf.getReleaseVersion());
                        ch.setRemainingBuf(null);
                    }
                    if (buffer.hasRemaining()) {
                        ByteBuf remaining = allocator.allocate(buffer.remaining());
                        remaining.read(buffer);
                        remaining.flip();
                        ch.setRemainingBuf(remaining);
                    }
                    return;
                }
            } catch (Throwable e) {
                future.release(ch.getChannelThreadContext());
                if (e instanceof IOException) {
                    throw (IOException) e;
                }
                throw new IOException("exception occurred when do decode," + e.getMessage(), e);
            }
            if (setFutureNull) {
                ch.setReadFuture(null);
            }
            future.release(ch.getChannelThreadContext());
            foreFutureAcceptor.accept(ch.getSession(), future);
        }
    }

    @Override
    public void failed(Throwable exc, AioSocketChannel channel) {
        if (exc instanceof AsynchronousCloseException) {
            //FIXME 产生该异常的原因是shutdownOutput后对方收到 read(-1)然后调用shutdownOutput,本地在收到read(-1)之前关闭了连接
            return;
        }
        logger.error(exc.getMessage() + ", channel:" + channel, exc);
        CloseUtil.close(channel);
    }

}
