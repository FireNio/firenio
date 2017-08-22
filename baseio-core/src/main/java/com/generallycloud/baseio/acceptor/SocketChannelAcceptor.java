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
package com.generallycloud.baseio.acceptor;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.generallycloud.baseio.component.AioSocketChannelContext;
import com.generallycloud.baseio.component.NioSocketChannelContext;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.protocol.ChannelFuture;
import com.generallycloud.baseio.protocol.Future;

/**
 * @author wangkai
 *
 */
public class SocketChannelAcceptor implements ChannelAcceptor {

    private AbstractSocketChannelAcceptor _channelAcceptor;

    public SocketChannelAcceptor(SocketChannelContext context) {
        this._channelAcceptor = buildConnector(context);
    }

    private AbstractSocketChannelAcceptor unwrap() {
        return _channelAcceptor;
    }

    @Override
    public void unbind() throws IOException {
        unwrap().unbind();
    }

    @Override
    public SocketChannelContext getContext() {
        return unwrap().getContext();
    }

    @Override
    public InetSocketAddress getServerSocketAddress() {
        return unwrap().getServerSocketAddress();
    }

    @Override
    public boolean isActive() {
        return unwrap().isActive();
    }

    @Override
    public void bind() throws IOException {
        unwrap().bind();
    }

    @Override
    public void broadcast(Future future) throws IOException {
        unwrap().broadcast(future);
    }

    @Override
    public int getManagedSessionSize() {
        return unwrap().getManagedSessionSize();
    }

    private AbstractSocketChannelAcceptor buildConnector(SocketChannelContext context) {
        if (context instanceof NioSocketChannelContext) {
            return new NioSocketChannelAcceptor(context);
        } else if (context instanceof AioSocketChannelContext) {
            return new AioSocketChannelAcceptor((AioSocketChannelContext) context);
        }
        throw new IllegalArgumentException("context");
    }

    @Override
    public void broadcastChannelFuture(ChannelFuture future) throws IOException {
        unwrap().broadcastChannelFuture(future);
    }

}
