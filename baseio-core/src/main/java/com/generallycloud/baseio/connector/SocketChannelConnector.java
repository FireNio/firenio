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
package com.generallycloud.baseio.connector;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.generallycloud.baseio.component.AioSocketChannelContext;
import com.generallycloud.baseio.component.NioSocketChannelContext;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSession;

/**
 * @author wangkai
 *
 */
public class SocketChannelConnector implements ChannelConnector {

    private AbstractSocketChannelConnector connector;

    private SocketChannelContext           context;

    public SocketChannelConnector(SocketChannelContext context) {
        this.context = context;
        this.connector = buildConnector(context);
    }

    private AbstractSocketChannelConnector unwrap() {
        return connector;
    }

    @Override
    public SocketSession getSession() {
        return unwrap().getSession();
    }

    @Override
    public SocketSession connect() throws IOException {
        return unwrap().connect();
    }

    @Override
    public SocketChannelContext getContext() {
        return context;
    }

    @Override
    public void close() throws IOException {
        unwrap().close();
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
    public boolean isConnected() {
        return unwrap().isConnected();
    }

    @Override
    public long getTimeout() {
        return unwrap().getTimeout();
    }

    @Override
    public void setTimeout(long timeout) {
        unwrap().setTimeout(timeout);
    }

    private AbstractSocketChannelConnector buildConnector(SocketChannelContext context) {
        context.addSessionEventListener(new CloseConnectorSEListener(this));
        if (context instanceof NioSocketChannelContext) {
            return new NioSocketChannelConnector((NioSocketChannelContext) context);
        } else if (context instanceof AioSocketChannelContext) {
            return new AioSocketChannelConnector((AioSocketChannelContext) context);
        }
        throw new IllegalArgumentException("context");
    }

    /**
     * @return the _connector
     */
    public AbstractSocketChannelConnector getConnector() {
        return unwrap();
    }

    protected void physicalClose() {
        unwrap().physicalClose();
    }

}
