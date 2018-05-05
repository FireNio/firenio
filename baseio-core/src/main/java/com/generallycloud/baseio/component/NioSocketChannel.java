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
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.baseio.ClosedChannelException;
import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.concurrent.LinkedQueue;
import com.generallycloud.baseio.protocol.ChannelFuture;

public class NioSocketChannel extends AbstractSocketChannel implements SelectorLoopEvent {

    private SocketChannel           channel;
    private NioSocketChannelContext context;
    private SelectionKey            selectionKey;
    private SelectorEventLoop       eventLoop;

    NioSocketChannel(SelectorEventLoop eventLoop, SelectionKey selectionKey, int channelId) {
        super(eventLoop, channelId);
        this.eventLoop = eventLoop;
        this.context = eventLoop.getChannelContext();
        this.selectionKey = selectionKey;
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
                NioSocketChannel.this.physicalClose();
            }
        });
    }

    @Override
    protected void doFlush0() {
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
        ChannelFuture future = writeFuture;
        LinkedQueue<ChannelFuture> writeFutures = this.writeFutures;
        if (future == null) {
            future = writeFutures.poll();
        }
        if (future == null) {
            return;
        }
        for (;;) {
            try {
                write(future);
            } catch (IOException e) {
                writeFuture = null;
                exceptionCaught(future, e);
                throw e;
            }
            if (!future.isWriteCompleted()) {
                writeFuture = future;
                interestWrite(selectionKey);
                return;
            }
            writeFutureLength(-future.getByteBufLimit());
            onFutureSent(future);
            future = writeFutures.poll();
            if (future == null) {
                break;
            }
        }
        interestRead(selectionKey);
        writeFuture = null;
    }

    @Override
    public NioSocketChannelContext getContext() {
        return context;
    }

    @Override
    protected InetSocketAddress getLocalSocketAddress0() throws IOException {
        return (InetSocketAddress) channel.getLocalAddress();
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
    protected SocketChannelThreadContext getSocketChannelThreadContext() {
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
    protected void physicalClose() {
        if (!isOpened()) {
            return;
        }
        ReentrantLock lock = getCloseLock();
        lock.lock();
        try {
            closeSSL();
            // 最后一轮 //FIXME once
            try {
                write(eventLoop);
            } catch (Exception e) {}
            releaseFutures();
            selectionKey.attach(null);
            CloseUtil.close(channel);
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

    @Override
    public void write(ByteBuf buf) throws IOException {
        int length = channel.write(buf.nioBuffer());
        if (length < 1) {
            return;
        }
        buf.reverse();
    }

}
