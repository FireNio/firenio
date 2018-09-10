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

import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.ThreadUtil;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public class ReConnector implements Closeable {

    private Logger           logger      = LoggerFactory.getLogger(getClass());
    private ChannelConnector connector   = null;
    private long             retryTime   = 15000;
    private volatile boolean reconnect   = true;
    private ReConnector      reConnector = null;

    public ReConnector(ChannelContext context) {
        context.addChannelEventListener(newReconnectSEListener());
        this.connector = new ChannelConnector(context);
        this.reConnector = this;
    }

    public ReConnector(ChannelContext context, NioEventLoop eventLoop) {
        context.addChannelEventListener(newReconnectSEListener());
        this.connector = new ChannelConnector(context, eventLoop);
        this.reConnector = this;
    }

    public ReConnector(ChannelContext context, NioEventLoopGroup group) {
        context.addChannelEventListener(newReconnectSEListener());
        this.connector = new ChannelConnector(context, group);
        this.reConnector = this;
    }

    public boolean isConnected() {
        return connector.isConnected();
    }

    public NioSocketChannel getChannel() {
        return connector.getChannel();
    }

    public synchronized void connect() {
        if (!reconnect) {
            logger.info("connection is closed, stop to reconnect");
            return;
        }
        NioSocketChannel ch = connector.getChannel();
        for (;;) {
            if (ch != null && ch.isOpened()) {
                break;
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

    private ChannelEventListenerAdapter newReconnectSEListener() {
        return new ChannelEventListenerAdapter() {
            @Override
            public void channelClosed(NioSocketChannel ch) {
                reconnect(reConnector);
            }
        };
    }

    private void reconnect(final ReConnector reconnectableConnector) {
        ThreadUtil.exec(new Runnable() {
            @Override
            public void run() {
                logger.info("begin try to reconnect");
                reconnectableConnector.connect();
            }
        });
    }

    @Override
    public synchronized void close() {
        reconnect = false;
        CloseUtil.close(connector);
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
