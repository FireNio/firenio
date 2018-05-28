/*
 * Copyright 2015-2017 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.generallycloud.baseio.component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketOption;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.baseio.ClosedChannelException;
import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.UnpooledByteBufAllocator;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.ReleaseUtil;
import com.generallycloud.baseio.component.ssl.SslHandler;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.ChannelFuture;

public class AioSocketChannel extends AbstractSocketChannel {

    private AsynchronousSocketChannel channel;
    private AioSocketChannelContext   context;
    private ReadCompletionHandler     readCompletionHandler;
    private WriteCompletionHandler    writeCompletionHandler;
    private ByteBuf                   readCache;
    private CachedAioThread           aioThread;
    private transient ChannelFuture   writeFuture;

    private static final Logger       logger = LoggerFactory.getLogger(AioSocketChannel.class);

    public AioSocketChannel(CachedAioThread aioThread, AsynchronousSocketChannel channel,
            int channelId) {
        super(aioThread, channelId);
        this.channel = channel;
        this.readCache = UnpooledByteBufAllocator.getHeap().allocate(4096);
        this.context = aioThread.getChannelContext();
        this.readCompletionHandler = aioThread.getReadCompletionHandler();
        this.writeCompletionHandler = aioThread.getWriteCompletionHandler();
        this.aioThread = aioThread;
    }

    @Override
    public AioSocketChannelContext getContext() {
        return context;
    }

    @Override
    public <T> T getOption(SocketOption<T> name) throws IOException {
        return channel.getOption(name);
    }

    @Override
    public <T> void setOption(SocketOption<T> name, T value) throws IOException {
        channel.setOption(name, value);
    }

    @Override
    protected void doFlush() {
        flush(aioThread);
    }

    @Override
    protected InetSocketAddress getRemoteSocketAddress0() {
        try {
            return (InetSocketAddress) channel.getRemoteAddress();
        } catch (Exception e) {
            return ERROR_SOCKET_ADDRESS;
        }
    }

    @Override
    protected InetSocketAddress getLocalSocketAddress0() {
        try {
            return (InetSocketAddress) channel.getLocalAddress();
        } catch (IOException e) {
            return ERROR_SOCKET_ADDRESS;
        }
    }

    private volatile boolean flushing;

    //当writeCompleteCallback时forceFlushing为true
    //此时可以再次write(writeFuture)
    private void flush(ChannelThreadContext context) {
        if (flushing) {
            return;
        }
        write(context);
    }

    private void write(ChannelThreadContext context) {
        try {
            if (writeFuture == null) {
                writeFuture = writeFutures.poll();
            }
            if (writeFuture == null) {
                flushing = false;
                return;
            }
            if (!isOpened()) {
                exceptionCaught(writeFuture, new ClosedChannelException("closed"));
                return;
            }
            flushing = true;
            if (writeFuture.isNeedSsl()) {
                writeFuture.setNeedSsl(false);
                // FIXME 部分情况下可以不在业务线程做wrapssl
                ByteBuf old = writeFuture.getByteBuf();
                long version = old.getReleaseVersion();
                SslHandler handler = context.getSslHandler();
                try {
                    ByteBuf newBuf = handler.wrap(this, old);
                    newBuf.nioBuffer();
                    writeFuture.setByteBuf(newBuf);
                } finally {
                    ReleaseUtil.release(old, version);
                }
            }
            channel.write(writeFuture.getByteBuf().nioBuffer(), this, writeCompletionHandler);
        } catch (IOException e) {
            exceptionCaught(writeFuture, e);
        }
    }

    // FIXME 这里有问题
    @Override
    protected void close0() {
        this.opened = false;
        this.closeSSL();
        // 最后一轮 //FIXME once
        this.flush(aioThread);
        ClosedChannelException ce = null;
        if (writeFuture != null && !writeFuture.isReleased()) {
            ce = new ClosedChannelException(session.toString());
            exceptionCaught(writeFuture, ce);
        }
        this.releaseFutures(ce);
        try {
            channel.shutdownOutput();
        } catch (IOException e) {
            logger.info(e.getMessage(), e);
        }
        try {
            channel.shutdownInput();
        } catch (IOException e) {
            logger.info(e.getMessage(), e);
        }
        CloseUtil.close(channel);
        fireClosed();
    }

    protected void read(ByteBuf cache) {
        channel.read(cache.clear().nioBuffer(), this, readCompletionHandler);
    }

    @Override
    public void close() throws IOException {
        ReentrantLock lock = getCloseLock();
        lock.lock();
        try {
            if (!isOpened()) {
                return;
            }
            close0();
        } finally {
            lock.unlock();
        }
    }

    protected void writeCallback(int length) {
        ReentrantLock lock = getCloseLock();
        lock.lock();
        try {
            if (!isOpened()) {
                return;
            }
            ChannelFuture f = this.writeFuture;
            f.getByteBuf().reverse();
            if (!f.isWriteCompleted()) {
                write(aioThread);
                return;
            }
            try {
                f.release(getChannelThreadContext());
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
            }
            try {
                aioThread.getIoEventHandle().futureSent(session, f);
            } catch (Throwable e) {
                logger.debug(e.getMessage(), e);
            }
            writeFuture = null;
            write(aioThread);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    protected ByteBuf getReadCache() {
        return readCache;
    }

    @Override
    public CachedAioThread getChannelThreadContext() {
        return aioThread;
    }

}
