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

import java.io.Closeable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.ThreadUtil;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.component.SocketSessionEventListenerAdapter;

public class ReconnectableConnector implements Closeable {

    private Logger                 logger                 = LoggerFactory
            .getLogger(ReconnectableConnector.class);
    private SocketChannelConnector realConnector          = null;
    private long                   retryTime              = 15000;
    private volatile boolean       reconnect              = true;
    private ReconnectableConnector reconnectableConnector = null;

    public ReconnectableConnector(SocketChannelContext context) {
        context.addSessionEventListener(getReconnectSEListener());
        this.realConnector = new SocketChannelConnector(context);
        this.reconnectableConnector = this;
    }

    public boolean isConnected() {
        return realConnector.isConnected();
    }

    public SocketSession getSession() {
        return realConnector.getSession();
    }

    public synchronized void connect() {
        if (!reconnect) {
            logger.info("connection is closed, stop to reconnect");
            return;
        }
        SocketSession session = realConnector.getSession();
        ThreadUtil.sleep(300);
        logger.info("begin try to connect");
        for (;;) {
            if (session != null && session.isOpened()) {
                logger.error("connection did not closed, reconnect later on");
                ThreadUtil.sleep(retryTime);
                continue;
            }
            try {
                realConnector.connect();
                break;
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
            }
            logger.error("reconnect failed,try reconnect later on");
            ThreadUtil.sleep(retryTime);
        }
    }

    private SocketSessionEventListenerAdapter getReconnectSEListener() {
        return new SocketSessionEventListenerAdapter() {
            @Override
            public void sessionClosed(SocketSession session) {
                reconnect(reconnectableConnector);
            }
        };
    }

    private void reconnect(final ReconnectableConnector reconnectableConnector) {
        ThreadUtil.execute(new Runnable() {
            @Override
            public void run() {
                logger.info("begin try to reconnect");
                reconnectableConnector.connect();
            }
        });
    }

    @Override
    public void close() {
        reconnect = false;
        synchronized (this) {
            CloseUtil.close(realConnector);
        }
    }

    public long getRetryTime() {
        return retryTime;
    }

    public void setRetryTime(long retryTime) {
        this.retryTime = retryTime;
    }

    /**
     * @return the realConnector
     */
    public SocketChannelConnector getRealConnector() {
        return realConnector;
    }

}
