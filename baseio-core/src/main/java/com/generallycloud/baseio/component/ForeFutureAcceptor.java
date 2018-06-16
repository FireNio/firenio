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

import java.util.List;

import com.generallycloud.baseio.concurrent.ExecutorEventLoop;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.Future;
import com.generallycloud.baseio.protocol.ProtocolCodec;

public class ForeFutureAcceptor {

    private Logger            logger = LoggerFactory.getLogger(getClass());

    protected HeartBeatLogger heartBeatLogger;

    protected final boolean   enableWorkEventLoop;

    public ForeFutureAcceptor(boolean enableWorkEventLoop) {
        this.enableWorkEventLoop = enableWorkEventLoop;
    }

    public void initialize(ChannelContext channelContext) throws Exception {
        createHeartBeatLogger(channelContext);
    }

    public void accept(final NioSocketChannel channel, List<Future> futures) {
        if (futures.isEmpty()) {
            return;
        }
        final ChannelContext context = channel.getContext();
        final IoEventHandle eventHandle = context.getIoEventHandle();
        for (int i = 0; i < futures.size(); i++) {
            final Future future = futures.get(i);
            if (future.isSilent()) {
                continue;
            }
            if (future.isHeartbeat()) {
                acceptHeartBeat(channel, future);
                continue;
            }
            if (enableWorkEventLoop) {
                ExecutorEventLoop eventLoop = channel.getExecutorEventLoop();
                eventLoop.dispatch(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            eventHandle.accept(channel, future);
                        } catch (Exception e) {
                            eventHandle.exceptionCaught(channel, future, e);
                        }
                    }
                });
            } else {
                try {
                    eventHandle.accept(channel, future);
                } catch (Exception e) {
                    eventHandle.exceptionCaught(channel, future, e);
                }
            }
        }
    }

    protected void acceptHeartBeat(final NioSocketChannel channel, final Future future) {
        if (future.isPING()) {
            heartBeatLogger.logRequest(channel);
            ProtocolCodec codec = channel.getProtocolCodec();
            Future f = codec.createPONGPacket(channel, future);
            if (f == null) {
                return;
            }
            channel.flush(f);
        } else {
            heartBeatLogger.logResponse(channel);
        }
    }

    protected void createHeartBeatLogger(ChannelContext context) {
        if (context.getConfiguration().isEnableHeartbeatLog()) {
            heartBeatLogger = new HeartBeatLogger() {
                @Override
                public void logRequest(NioSocketChannel channel) {
                    logger.info("heart beat request from: {}", channel);
                }

                @Override
                public void logResponse(NioSocketChannel channel) {
                    logger.info("heart beat response from: {}", channel);
                }
            };
        } else {
            heartBeatLogger = new HeartBeatLogger() {
                @Override
                public void logRequest(NioSocketChannel channel) {
                    logger.debug("heart beat request from: {}", channel);
                }

                @Override
                public void logResponse(NioSocketChannel channel) {
                    logger.debug("heart beat response from: {}", channel);
                }
            };
        }
    }

    private interface HeartBeatLogger {

        void logRequest(NioSocketChannel channel);

        void logResponse(NioSocketChannel channel);
    }

}
