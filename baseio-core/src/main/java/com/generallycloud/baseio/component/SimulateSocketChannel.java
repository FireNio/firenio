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

import com.generallycloud.baseio.buffer.ByteBuf;
import com.generallycloud.baseio.buffer.ByteBufAllocator;
import com.generallycloud.baseio.buffer.UnpooledByteBufAllocator;
import com.generallycloud.baseio.component.ssl.SslHandler;
import com.generallycloud.baseio.concurrent.ExecutorEventLoop;

/**
 * @author wangkai
 *
 */
public class SimulateSocketChannel extends AbstractSocketChannel{

    private SocketChannelContext context;
    private SocketSelectorEventLoop selectorEventLoop;

    SimulateSocketChannel(final SocketChannelContext context) {
        super(new SocketChannelThreadContext() {
            
            @Override
            public boolean inEventLoop() {
                throw new UnsupportedOperationException();
            }
            
            @Override
            public SslHandler getSslHandler() {
                throw new UnsupportedOperationException();
            }
            
            @Override
            public SocketSessionManager getSocketSessionManager() {
                return context.getSessionManager();
            }
            
            @Override
            public ExecutorEventLoop getExecutorEventLoop() {
                return null;
            }
            
            @Override
            public SocketChannelContext getChannelContext() {
                return context;
            }
            
            @Override
            public ByteBufAllocator getByteBufAllocator() {
                return UnpooledByteBufAllocator.getHeap();
            }
        } , -1);
        this.context = context;
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    protected void doFlush0() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SocketChannelContext getContext() {
        return context;
    }

    @Override
    protected InetSocketAddress getLocalSocketAddress0() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T getOption(SocketOption<T> name) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected InetSocketAddress getRemoteSocketAddress0() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected SocketChannelThreadContext getSocketChannelThreadContext() {
        return selectorEventLoop;
    }

    @Override
    public boolean isBlocking() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void physicalClose() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void setOption(SocketOption<T> name, T value) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(ByteBuf buf) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void fireClosed() {
    }

    @Override
    public void fireOpend() {
    }

}
