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

import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSessionManager;
import com.generallycloud.baseio.protocol.ChannelFuture;
import com.generallycloud.baseio.protocol.Future;

/**
 * @author wangkai
 */
public abstract class AbstractSocketChannelAcceptor extends AbstractChannelAcceptor {

    private SocketChannelContext context;

    private SocketSessionManager socketSessionManager;

    AbstractSocketChannelAcceptor(SocketChannelContext context) {
        this.context = context;
        this.socketSessionManager = context.getSessionManager();
    }

    @Override
    public SocketChannelContext getContext() {
        return context;
    }

    @Override
    public void broadcast(Future future) throws IOException {
        context.getSessionManager().broadcast(future);
    }

    @Override
    public void broadcastChannelFuture(ChannelFuture future) throws IOException {
        context.getSessionManager().broadcastChannelFuture(future);
    }

    @Override
    public int getManagedSessionSize() {
        return socketSessionManager.getManagedSessionSize();
    }
}
