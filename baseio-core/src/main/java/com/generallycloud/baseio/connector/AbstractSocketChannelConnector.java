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

import com.generallycloud.baseio.TimeoutException;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.MessageFormatter;
import com.generallycloud.baseio.component.AbstractChannelService;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.component.UnsafeSocketSession;
import com.generallycloud.baseio.concurrent.Waiter;
import com.generallycloud.baseio.configuration.ServerConfiguration;

/**
 * @author wangkai
 *
 */
public abstract class AbstractSocketChannelConnector extends AbstractChannelService
        implements ChannelConnector {

    private UnsafeSocketSession session;
    private long                timeout          = 3000;
    private Waiter              waiter;

    @Override
    public synchronized void close() throws IOException {
        if (getSession() != null) {
            CloseUtil.close(getSession());
        }
        stop();
    }

    @Override
    public synchronized SocketSession connect() throws IOException {
        this.session = null;
        this.initialize();
        return getSession();
    }

    protected abstract void connect(InetSocketAddress server) throws IOException;

    protected abstract void connected();

    //FIXME protected
    public void finishConnect(UnsafeSocketSession session, Throwable exception) {
        Waiter waiter = this.waiter;
        if (waiter == null) {
            CloseUtil.close(session);
            return;
        }
        if (exception != null) {
            waiter.response(exception);
        }else{
            waiter.response(session);
        }
        if (waiter.isTimeouted()) {
            CloseUtil.close(session);
        }
    }

    @Override
    public SocketSession getSession() {
        return session;
    }

    @Override
    public long getTimeout() {
        return timeout;
    }

    @Override
    protected void initService(ServerConfiguration configuration) throws IOException {
        String SERVER_HOST = configuration.getSERVER_HOST();
        int SERVER_PORT = configuration.getSERVER_PORT();
        this.waiter = new Waiter();
        this.serverAddress = new InetSocketAddress(SERVER_HOST, SERVER_PORT);
        this.connect(getServerSocketAddress());
    }

    @Override
    public boolean isActive() {
        return isConnected();
    }

    @Override
    public boolean isConnected() {
        return getSession() != null && getSession().isOpened();
    }

    @Override
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    protected void wait4connect() throws IOException {
        if(waiter.await(timeout)){
            CloseUtil.close(this);
            throw new TimeoutException("connect to " + getServerSocketAddress() + " time out");
        }
        if (waiter.isFailed()) {
            CloseUtil.close(this);
            Throwable ex = (Throwable) waiter.getResponse();
            String errorMsg = MessageFormatter.format(
                    "connect to [{}] failed,nested exception is {}", getServerSocketAddress(),
                    ex.getMessage());
            throw new IOException(errorMsg, ex);
        }
        this.session = (UnsafeSocketSession) waiter.getResponse();
        this.connected();
        this.waiter = null;
    }

}
