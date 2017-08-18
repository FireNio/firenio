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

import com.generallycloud.baseio.TimeoutException;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.LoggerUtil;
import com.generallycloud.baseio.common.MessageFormatter;
import com.generallycloud.baseio.common.ThreadUtil;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.component.UnsafeSocketSession;
import com.generallycloud.baseio.log.Logger;

/**
 * @author wangkai
 *
 */
public abstract class AbstractSocketChannelConnector extends AbstractChannelConnector {

    protected UnsafeSocketSession session;

    private Throwable             connectException;

    private boolean               timeouted;

    private Object                wait4ConnectLock = new Object();

    @Override
    public SocketSession getSession() {
        return session;
    }

    @Override
    protected boolean canSafeClose() {
        return session == null
                || (!session.inSelectorLoop() && !session.getExecutorEventLoop().inEventLoop());
    }

    //FIXME protected
    public void finishConnect(UnsafeSocketSession session, Throwable exception) {
        synchronized (wait4ConnectLock) {
            if (timeouted) {
                CloseUtil.close(session);
                wait4ConnectLock.notify();
                return;
            }
            if (session == null) {
                connectException = exception;
            } else {
                this.session = session;
                LoggerUtil.prettyLog(getLogger(), "connected to server @{}",
                        getServerSocketAddress());
            }
            wait4ConnectLock.notify();
        }
    }

    @Override
    public synchronized SocketSession connect() throws IOException {
        this.session = null;
        this.initialize();
        return getSession();
    }

    protected void wait4connect() throws TimeoutException {
        timeouted = false;
        connectException = null;
        synchronized (wait4ConnectLock) {
            ThreadUtil.wait(wait4ConnectLock, timeout);
        }
        if (getSession() == null) {
            timeouted = true;
            CloseUtil.close(this);
            if (connectException == null) {
                throw new TimeoutException(
                        "connect to " + getServerSocketAddress().toString() + " time out");
            } else {
                throw new TimeoutException(
                        MessageFormatter.format(
                                "connect faild,connector:[{}],nested exception is {}",
                                getServerSocketAddress(), connectException.getMessage()),
                        connectException);
            }
        }
    }

    abstract Logger getLogger();

}
