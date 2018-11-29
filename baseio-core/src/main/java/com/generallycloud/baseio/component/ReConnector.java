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
package com.generallycloud.baseio.component;

import java.io.Closeable;

import com.generallycloud.baseio.LifeCycleUtil;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.ThreadUtil;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public class ReConnector implements Closeable {

    private Logger           logger      = LoggerFactory.getLogger(getClass());
    private ChannelConnector connector   = null;
    private long             retryTime   = 15000;
    private volatile boolean reconnect   = true;

    public ReConnector(ChannelConnector connector) {
        this.connector = connector;
        this.init();
    }
    
    @SuppressWarnings("resource")
    private void init(){
        final ReConnector reConnector = this;
        this.connector.getProcessorGroup().setSharable(true);
        this.connector.addChannelEventListener(new ChannelEventListenerAdapter() {
            @Override
            public void channelClosed(NioSocketChannel ch) {
                ThreadUtil.exec(new Runnable() {
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

    public NioSocketChannel getChannel() {
        return connector.getChannel();
    }

    public synchronized void connect() {
        NioSocketChannel ch = connector.getChannel();
        for (;;) {
            if (ch != null && ch.isOpened()) {
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
            ThreadUtil.sleep(retryTime);
        }
    }

    @Override
    public synchronized void close() {
        this.reconnect = false;
        CloseUtil.close(connector);
        LifeCycleUtil.stop(connector.getProcessorGroup());
    }

    public long getRetryTime() {
        return retryTime;
    }

    public void setRetryTime(long retryTime) {
        this.retryTime = retryTime;
    }

    public ChannelConnector getRealConnector() {
        return connector;
    }
    
    @Override
    public String toString() {
        return connector.toString();
    }

}
