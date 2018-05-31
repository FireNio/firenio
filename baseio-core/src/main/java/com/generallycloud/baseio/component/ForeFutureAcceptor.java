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
import com.generallycloud.baseio.protocol.ChannelFuture;
import com.generallycloud.baseio.protocol.Future;
import com.generallycloud.baseio.protocol.ProtocolCodec;

public class ForeFutureAcceptor {

    private Logger          logger = LoggerFactory.getLogger(getClass());

    protected HeartBeatLogger heartBeatLogger;

    protected final boolean   enableWorkEventLoop;

    public ForeFutureAcceptor(boolean enableWorkEventLoop) {
        this.enableWorkEventLoop = enableWorkEventLoop;
    }

    public void initialize(ChannelContext channelContext) throws Exception {
        createHeartBeatLogger(channelContext);
    }

    public void accept(final SocketSession session, List<ChannelFuture> futures) {
        if (futures.isEmpty()) {
            return;
        }
        final ChannelContext context = session.getContext();
        final IoEventHandle eventHandle = context.getIoEventHandle();
        for (int i = 0; i < futures.size(); i++) {
            final ChannelFuture future = futures.get(i);
            if (future.isSilent()) {
                continue;
            }
            if (future.isHeartbeat()) {
                acceptHeartBeat(session, future);
                continue;
            }
            if (enableWorkEventLoop) {
                ExecutorEventLoop eventLoop = session.getExecutorEventLoop();
                eventLoop.dispatch(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            eventHandle.accept(session, future);
                        } catch (Exception e) {
                            eventHandle.exceptionCaught(session, future, e);
                        }
                    }
                });
            } else {
                try {
                    eventHandle.accept(session, future);
                } catch (Exception e) {
                    eventHandle.exceptionCaught(session, future, e);
                }
            }
        }
        futures.clear();
    }

    protected void acceptHeartBeat(final SocketSession session, final ChannelFuture future) {
        if (future.isPING()) {
            heartBeatLogger.logRequest(session);
            ProtocolCodec codec = session.getProtocolCodec();
            Future f = codec.createPONGPacket(session, future);
            if (f == null) {
                return;
            }
            session.flush(f);
        } else {
            heartBeatLogger.logResponse(session);
        }
    }

    protected void createHeartBeatLogger(ChannelContext context) {
        if (context.getConfiguration().isEnableHeartbeatLog()) {
            heartBeatLogger = new HeartBeatLogger() {
                @Override
                public void logRequest(SocketSession session) {
                    logger.info("heart beat request from: {}", session);
                }

                @Override
                public void logResponse(SocketSession session) {
                    logger.info("heart beat response from: {}", session);
                }
            };
        } else {
            heartBeatLogger = new HeartBeatLogger() {
                @Override
                public void logRequest(SocketSession session) {
                    logger.debug("heart beat request from: {}", session);
                }

                @Override
                public void logResponse(SocketSession session) {
                    logger.debug("heart beat response from: {}", session);
                }
            };
        }
    }

    private interface HeartBeatLogger {

        void logRequest(SocketSession session);

        void logResponse(SocketSession session);
    }

}
