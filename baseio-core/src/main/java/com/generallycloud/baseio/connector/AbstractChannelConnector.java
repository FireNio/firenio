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

import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.ThreadUtil;
import com.generallycloud.baseio.component.AbstractChannelService;
import com.generallycloud.baseio.configuration.ServerConfiguration;

public abstract class AbstractChannelConnector extends AbstractChannelService
        implements ChannelConnector {

    protected long timeout = 3000;

    @Override
    public synchronized void close() throws IOException {
        if (canSafeClose()) {
            close0();
            if (isActive()) {
                ThreadUtil.wait(this);
            }
        } else {
            ThreadUtil.execute(new Runnable() {
                @Override
                public void run() {
                    close0();
                }
            });
        }
    }

    protected void physicalClose() {
        if (canSafeClose()) {
            destroy();
        } else {
            ThreadUtil.execute(new Runnable() {
                @Override
                public void run() {
                    destroy();
                }
            });
        }
    }

    private void close0() {
        if (getSession() == null) {
            physicalClose();
        } else {
            CloseUtil.close(getSession());
        }
    }

    protected abstract boolean canSafeClose();

    @Override
    protected void initService(ServerConfiguration configuration) throws IOException {
        String SERVER_HOST = configuration.getSERVER_HOST();
        int SERVER_PORT = configuration.getSERVER_PORT();
        this.serverAddress = new InetSocketAddress(SERVER_HOST, SERVER_PORT);
        this.connect(getServerSocketAddress());
    }

    protected abstract void connect(InetSocketAddress socketAddress) throws IOException;

    @Override
    public boolean isConnected() {
        return getSession() != null && getSession().isOpened();
    }

    @Override
    public boolean isActive() {
        return isConnected();
    }

    @Override
    public long getTimeout() {
        return timeout;
    }

    @Override
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    @Override
    protected void setServerCoreSize(ServerConfiguration configuration) {
        configuration.setSERVER_CORE_SIZE(1);
    }

}
