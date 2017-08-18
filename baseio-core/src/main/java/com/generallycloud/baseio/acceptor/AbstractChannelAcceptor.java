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

import com.generallycloud.baseio.TimeoutException;
import com.generallycloud.baseio.common.LoggerUtil;
import com.generallycloud.baseio.component.AbstractChannelService;
import com.generallycloud.baseio.configuration.ServerConfiguration;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public abstract class AbstractChannelAcceptor extends AbstractChannelService
        implements ChannelAcceptor {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public synchronized void bind() throws IOException {
        this.initialize();
    }

    @Override
    protected void initService(ServerConfiguration configuration) throws IOException {
        this.serverAddress = new InetSocketAddress(configuration.getSERVER_PORT());
        this.bind(getServerSocketAddress());
        LoggerUtil.prettyLog(logger, "server listening @{}", getServerSocketAddress());
    }

    protected abstract void bind(InetSocketAddress socketAddress) throws IOException;

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void unbind() throws TimeoutException {
        destroy();
    }

    @Override
    protected void setServerCoreSize(ServerConfiguration configuration) {
        if (this instanceof DatagramChannelAcceptor) {
            configuration.setSERVER_CORE_SIZE(1);
        }
    }

}
