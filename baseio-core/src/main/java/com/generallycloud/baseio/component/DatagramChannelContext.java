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

import java.math.BigDecimal;

import com.generallycloud.baseio.LifeCycleUtil;
import com.generallycloud.baseio.buffer.PooledByteBufAllocatorManager;
import com.generallycloud.baseio.common.LoggerUtil;
import com.generallycloud.baseio.configuration.ServerConfiguration;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public class DatagramChannelContext extends AbstractChannelContext {

    private DatagramSessionManager sessionManager;
    private DatagramPacketAcceptor datagramPacketAcceptor;
    private Logger                 logger = LoggerFactory.getLogger(getClass());

    public DatagramChannelContext(ServerConfiguration configuration) {
        super(configuration);
        this.sessionManager = new DatagramSessionManager(this);
    }

    @Override
    protected void doStart() throws Exception {

        this.clearContext();

        this.serverConfiguration.initializeDefault(this);

        int SERVER_CORE_SIZE = serverConfiguration.getSERVER_CORE_SIZE();
        int server_port = serverConfiguration.getSERVER_PORT();
        long session_idle = serverConfiguration.getSERVER_SESSION_IDLE_TIME();

        long SERVER_MEMORY_POOL_CAPACITY = serverConfiguration.getSERVER_MEMORY_POOL_CAPACITY()
                * SERVER_CORE_SIZE;
        long SERVER_MEMORY_POOL_UNIT = serverConfiguration.getSERVER_MEMORY_POOL_UNIT();

        double MEMORY_POOL_SIZE = new BigDecimal(
                SERVER_MEMORY_POOL_CAPACITY * SERVER_MEMORY_POOL_UNIT)
                        .divide(new BigDecimal(1024 * 1024), 2, BigDecimal.ROUND_HALF_UP)
                        .doubleValue();

        this.encoding = serverConfiguration.getSERVER_ENCODING();
        this.sessionIdleTime = serverConfiguration.getSERVER_SESSION_IDLE_TIME();

        if (getByteBufAllocatorManager() == null) {

            this.byteBufAllocatorManager = new PooledByteBufAllocatorManager(this);
        }

        LoggerUtil.prettyLog(logger,
                "======================================= service begin to start =======================================");
        LoggerUtil.prettyLog(logger, "encoding              ：{ {} }", encoding);
        LoggerUtil.prettyLog(logger, "cpu size              ：{ cpu * {} }", SERVER_CORE_SIZE);
        LoggerUtil.prettyLog(logger, "session idle          ：{ {} }", session_idle);
        LoggerUtil.prettyLog(logger, "listen port(udp)      ：{ {} }", server_port);
        LoggerUtil.prettyLog(logger, "memory pool cap       ：{ {} * {} ≈ {} M }", new Object[] {
                SERVER_MEMORY_POOL_UNIT, SERVER_MEMORY_POOL_CAPACITY, MEMORY_POOL_SIZE });

        LifeCycleUtil.start(byteBufAllocatorManager);
    }

    @Override
    protected void doStop() throws Exception {
        LifeCycleUtil.stop(byteBufAllocatorManager);
        sessionManager.stop();
    }

    public DatagramPacketAcceptor getDatagramPacketAcceptor() {
        return datagramPacketAcceptor;
    }

    public void setDatagramPacketAcceptor(DatagramPacketAcceptor datagramPacketAcceptor) {
        this.datagramPacketAcceptor = datagramPacketAcceptor;
    }

    public DatagramSessionManager getSessionManager() {
        return sessionManager;
    }

}
