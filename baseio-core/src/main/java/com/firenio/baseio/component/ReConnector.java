/*
 * Copyright 2015 The Baseio Project
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
package com.firenio.baseio.component;

import java.io.Closeable;

import com.firenio.baseio.common.Util;
import com.firenio.baseio.log.Logger;
import com.firenio.baseio.log.LoggerFactory;

public class ReConnector implements Closeable {

    private ChannelConnector connector = null;
    private Logger           logger    = LoggerFactory.getLogger(getClass());
    private volatile boolean reconnect = true;
    private long             retryTime = 15000;

    public ReConnector(ChannelConnector connector) {
        this.connector = connector;
        this.init();
    }

    @Override
    public synchronized void close() {
        this.reconnect = false;
        Util.close(connector);
        Util.stop(connector.getProcessorGroup());
    }

    public synchronized void connect() {
        Channel ch = connector.getChannel();
        for (;;) {
            if (ch != null && ch.isOpen()) {
                break;
            }
            if (!reconnect) {
                logger.info("connection is closed, stop to reconnect");
                return;
            }
            logger.info("begin try to connect");
            try {
                connector.connect();
                break;
            } catch (Throwable e) {
                logger.error(e.getMessage(), e);
            }
            logger.error("reconnect failed,try reconnect later on {} milliseconds", retryTime);
            Util.sleep(retryTime);
        }
    }

    public Channel getChannel() {
        return connector.getChannel();
    }

    public ChannelConnector getRealConnector() {
        return connector;
    }

    public long getRetryTime() {
        return retryTime;
    }

    @SuppressWarnings("resource")
    private void init() {
        final ReConnector reConnector = this;
        this.connector.addChannelEventListener(new ChannelEventListenerAdapter() {
            @Override
            public void channelClosed(Channel ch) {
                Util.exec(new Runnable() {
                    @Override
                    public void run() {
                        reConnector.connect();
                    }
                });
            }
        });
    }

    public boolean isConnected() {
        return connector.isConnected();
    }

    public void setRetryTime(long retryTime) {
        this.retryTime = retryTime;
    }

    @Override
    public String toString() {
        return connector.toString();
    }

}
