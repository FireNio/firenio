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
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.baseio.ClosedChannelException;
import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.ReleaseUtil;
import com.generallycloud.baseio.component.ssl.SslHandler;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.ChannelFuture;

public class NioSocketChannel extends AbstractSocketChannel implements SelectorLoopEvent {
    
    private static final Logger   logger = LoggerFactory.getLogger(NioSocketChannel.class);
    private SocketChannel           channel;
    private SelectionKey            selectionKey;
    private SelectorEventLoop       eventLoop;
    private ChannelFuture []        currentWriteFutures;
    private int                     currentWriteFuturesLen;

    NioSocketChannel(SelectorEventLoop eventLoop, SelectionKey selectionKey, int channelId) {
        super(eventLoop, channelId);
        int wbs = eventLoop.getChannelContext().getConfiguration().getWriteBuffers();
        this.eventLoop = eventLoop;
        this.selectionKey = selectionKey;
        this.currentWriteFutures  = new ChannelFuture[wbs];
        this.channel = (SocketChannel) selectionKey.channel();
    }

    @Override
    public void close() throws IOException {
        if (!isOpened()) {
            return;
        }
        eventLoop.dispatch(new SelectorLoopEvent() {
            
            @Override
            public void close() throws IOException { }
            
            @Override
            public void fireEvent(SelectorEventLoop selectorLoop) {
                NioSocketChannel.this.close0();
            }
        });
    }

    @Override
    protected void doFlush() {
        eventLoop.dispatch(this);
    }

    @Override
    public void fireEvent(SelectorEventLoop eventLoop) throws IOException {
        if (!isOpened()) {
            throw new ClosedChannelException("closed");
        }
        write(eventLoop);
    }

    protected void write(SelectorEventLoop eventLoop) throws IOException {
        ChannelFuture [] currentWriteFutures = this.currentWriteFutures;
        int maxLen = currentWriteFutures.length;
        for(;;){
            int i = currentWriteFuturesLen;
            for(;i< maxLen ; i ++){
                ChannelFuture future = writeFutures.poll();
                if (future == null) {
                    break;
                }
                currentWriteFutures[i] = future;
            }
            currentWriteFuturesLen = i;
            if (currentWriteFuturesLen == 0) {
                interestRead(selectionKey);
                return;
            }
            //FIXME ...是否要清空buffers
            ByteBuffer [] writeBuffers = eventLoop.getWriteBuffers();
            for (i = 0; i < currentWriteFuturesLen; i++) {
                ChannelFuture future = currentWriteFutures[i];
                if (future.isNeedSsl()) {
                    future.setNeedSsl(false);
                    // FIXME 部分情况下可以不在业务线程做wrapssl
                    ByteBuf old = future.getByteBuf();
                    long version = old.getReleaseVersion(); 
                    SslHandler handler = eventLoop.getSslHandler();
                    try {
                        ByteBuf newBuf = handler.wrap(this, old);
                        newBuf.nioBuffer();
                        future.setByteBuf(newBuf);
                    } finally {
                        ReleaseUtil.release(old,version);
                    }
                }
                writeBuffers[i] = future.getByteBuf().nioBuffer();
            }
            IoEventHandle ioEventHandle = eventLoop.getIoEventHandle();
            if (currentWriteFuturesLen == 1) {
                ByteBuffer nioBuf = writeBuffers[0];
                channel.write(nioBuf);
                if (nioBuf.hasRemaining()) {
                    currentWriteFutures[0].getByteBuf().reverse();
                    interestWrite(selectionKey);
                    return;
                }else{
                    ChannelFuture future = currentWriteFutures[0];
                    currentWriteFutures[0] = null;
                    try {
                        future.release(getChannelThreadContext());
                    } catch (Throwable e) {
                        logger.error(e.getMessage(), e);
                    }
                    try {
                        ioEventHandle.futureSent(session, future);
                    } catch (Throwable e) {
                        logger.debug(e.getMessage(), e);
                    }
                    currentWriteFuturesLen = 0;
                    interestRead(selectionKey);
                    return;
                }
            }else{
                channel.write(writeBuffers, 0, currentWriteFuturesLen);
                for (i = 0; i < currentWriteFuturesLen; i++) {
                    ChannelFuture future = currentWriteFutures[i];
                    if (writeBuffers[i].hasRemaining()) {
                        int remain = currentWriteFuturesLen - i;
                        if (remain > 16) {
                            System.arraycopy(currentWriteFutures, i, currentWriteFutures, 0, remain);
                        }else{
                            for (int j = 0; j < remain; j++) {
                                currentWriteFutures[j] = currentWriteFutures[i+j];
                            }
                        }
                        for (int j = currentWriteFuturesLen - i; j < maxLen; j++) {
                            currentWriteFutures[j] = null;
                        }
                        future.getByteBuf().reverse();
                        currentWriteFuturesLen = remain;
                        interestWrite(selectionKey);
                        return;
                    }else{
                        try {
                            future.release(getChannelThreadContext());
                        } catch (Throwable e) {
                            logger.error(e.getMessage(), e);
                        }
                        try {
                            ioEventHandle.futureSent(session, future);
                        } catch (Throwable e) {
                            logger.debug(e.getMessage(), e);
                        }
                    }
                }
                for (int j = 0; j < currentWriteFuturesLen; j++) {
                    currentWriteFutures[j] = null;
                }
                if (currentWriteFuturesLen != maxLen) {
                    currentWriteFuturesLen = 0;
                    interestRead(selectionKey);
                    return;
                }
                currentWriteFuturesLen = 0;
            }
        }
    }

    @Override
    public NioSocketChannelContext getContext() {
        return eventLoop.getChannelContext();
    }

    @Override
    protected InetSocketAddress getLocalSocketAddress0() {
        try {
            return (InetSocketAddress) channel.getLocalAddress();
        } catch (IOException e) {
            return ERROR_SOCKET_ADDRESS;
        }
    }

    @Override
    public <T> T getOption(SocketOption<T> name) throws IOException {
        return channel.getOption(name);
    }

    @Override
    protected InetSocketAddress getRemoteSocketAddress0() {
        try {
            return (InetSocketAddress) channel.getRemoteAddress();
        } catch (Exception e) {
        }
        return ERROR_SOCKET_ADDRESS;
    }

    @Override
    public ChannelThreadContext getChannelThreadContext() {
        return eventLoop;
    }

    private void interestRead(SelectionKey key) {
        int interestOps = key.interestOps();
        if (SelectionKey.OP_READ != interestOps) {
            key.interestOps(SelectionKey.OP_READ);
        }
    }

    private void interestWrite(SelectionKey key) {
        if ((SelectionKey.OP_READ | SelectionKey.OP_WRITE) != key.interestOps()) {
            key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        }
    }

    @Override
    public boolean isBlocking() {
        return channel.isBlocking();
    }

    // FIXME 这里有问题
    @Override
    protected void close0() {
        if (!isOpened()) {
            return;
        }
        ReentrantLock lock = getCloseLock();
        lock.lock();
        try {
            closeSSL();
            try {
                write(eventLoop);
            } catch (Exception e) {}
            ClosedChannelException e = null;
            if (currentWriteFuturesLen > 0) {
                e = new ClosedChannelException(session.toString());
                for (int i = 0; i < currentWriteFuturesLen; i++) {
                    exceptionCaught(currentWriteFutures[i], e);
                }
            }
            releaseFutures(e);
            CloseUtil.close(channel);
            selectionKey.attach(null);
            selectionKey.cancel();
            fireClosed();
            opened = false;
        } finally {
            lock.unlock();
        }
    }

    protected int read(ByteBuf buf) throws IOException {
        int length = channel.read(buf.getNioBuffer());
        if (length > 0) {
            buf.reverse();
        }
        return length;
    }

    @Override
    public <T> void setOption(SocketOption<T> name, T value) throws IOException {
        channel.setOption(name, value);
    }

}
