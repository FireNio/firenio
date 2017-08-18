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

import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.ChannelFuture;
import com.generallycloud.baseio.protocol.Future;

public abstract class AbstractFutureAcceptor implements ForeFutureAcceptor {

    private Logger          logger = LoggerFactory.getLogger(getClass());

    private HeartBeatLogger heartBeatLogger;

    @Override
    public void initialize(SocketChannelContext channelContext) throws Exception {
        createHeartBeatLogger(channelContext);
    }

    @Override
    public void accept(final SocketSession session, final Future future) throws Exception {

        ChannelFuture f = (ChannelFuture) future;

        if (f.isSilent()) {
            return;
        }

        if (f.isHeartbeat()) {

            acceptHeartBeat(session, f);

            return;
        }

        SocketChannelContext context = session.getContext();

        IoEventHandle eventHandle = context.getIoEventHandleAdaptor();

        accept(eventHandle, session, f);
    }

    protected abstract void accept(IoEventHandle eventHandle, SocketSession session,
            ChannelFuture future);

    private void acceptHeartBeat(final SocketSession session, final ChannelFuture future) {

        if (future.isPING()) {

            heartBeatLogger.logRequest(session);

            SocketChannelContext context = session.getContext();

            BeatFutureFactory factory = context.getBeatFutureFactory();

            if (factory == null) {

                RuntimeException e = new RuntimeException("none factory of BeatFuture");

                CloseUtil.close(session);

                logger.error(e.getMessage(), e);

                return;
            }

            Future f = factory.createPONGPacket(session);

            session.flush(f);
        } else {
            heartBeatLogger.logResponse(session);
        }

    }

    private void createHeartBeatLogger(SocketChannelContext context) {

        if (context.getServerConfiguration().isSERVER_ENABLE_HEARTBEAT_LOG()) {
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
