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

import com.generallycloud.baseio.LifeCycleUtil;
import com.generallycloud.baseio.TimeoutException;
import com.generallycloud.baseio.component.SocketSessionManager;
import com.generallycloud.baseio.protocol.ChannelFuture;
import com.generallycloud.baseio.protocol.Future;

/**
 * @author wangkai
 */
public abstract class AbstractChannelAcceptor implements ChannelAcceptor {

    private boolean              active        = false;
    private InetSocketAddress    serverAddress;
    private SocketSessionManager sessionManager;

    @Override
    public synchronized void bind() throws IOException {
        if (isActive()) {
            return;
        }
        if (getContext() == null) {
            throw new NullPointerException("null context");
        }
        LifeCycleUtil.stop(getContext());
        getContext().setChannelService(this);
        LifeCycleUtil.start(getContext());
        int port = getContext().getConfiguration().getPort();
        this.serverAddress = new InetSocketAddress(port);
        this.bind(getServerSocketAddress());
        this.sessionManager = getContext().getSessionManager();
        this.active = true;
    }

    protected abstract void bind(InetSocketAddress server) throws IOException;

    @Override
    public void broadcast(Future future) throws IOException {
        sessionManager.broadcast(future);
    }

    @Override
    public void broadcastChannelFuture(ChannelFuture future) throws IOException {
        sessionManager.broadcastChannelFuture(future);
    }

    @Override
    public InetSocketAddress getServerSocketAddress() {
        return serverAddress;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    protected abstract void unbind0();

    @Override
    public synchronized void unbind() throws TimeoutException {
        active = false;
        unbind0();
        LifeCycleUtil.stop(getContext());
    }

}
