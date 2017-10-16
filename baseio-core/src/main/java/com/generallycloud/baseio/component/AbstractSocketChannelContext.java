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
import com.generallycloud.baseio.common.LoggerUtil;
import com.generallycloud.baseio.component.ssl.SslContext;
import com.generallycloud.baseio.concurrent.ExecutorEventLoopGroup;
import com.generallycloud.baseio.concurrent.FixedAtomicInteger;
import com.generallycloud.baseio.concurrent.LineEventLoopGroup;
import com.generallycloud.baseio.concurrent.ThreadEventLoopGroup;
import com.generallycloud.baseio.configuration.ServerConfiguration;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.EmptyFuture;
import com.generallycloud.baseio.protocol.ProtocolDecoder;
import com.generallycloud.baseio.protocol.ProtocolEncoder;
import com.generallycloud.baseio.protocol.ProtocolFactory;

public abstract class AbstractSocketChannelContext extends AbstractChannelContext
        implements SocketChannelContext {

    private IoEventHandleAdaptor                                 ioEventHandleAdaptor;
    private ProtocolFactory                                      protocolFactory;
    private BeatFutureFactory                                    beatFutureFactory;
    private ExecutorEventLoopGroup                               executorEventLoopGroup;
    private ProtocolEncoder                                      protocolEncoder;
    private ProtocolDecoder                                      protocolDecoder;
    private SslContext                                           sslContext;
    private boolean                                              enableSSL;
    private boolean                                              initialized;
    private ForeFutureAcceptor                                   foreReadFutureAcceptor;
    private SocketSessionFactory                                 sessionFactory;
    private ChannelByteBufReaderLinkGroup                        channelByteBufReaderGroup     = new ChannelByteBufReaderLinkGroup();
    private LinkableGroup<SocketSessionEventListenerWrapper>     sessionEventListenerGroup     = new LinkableGroup<>();
    private LinkableGroup<SocketSessionIdleEventListenerWrapper> sessionIdleEventListenerGroup = new LinkableGroup<>();
    private Logger                                               logger                        = LoggerFactory
            .getLogger(getClass());
    private FixedAtomicInteger                                   CHANNEL_ID;

    @Override
    public void addSessionEventListener(SocketSessionEventListener listener) {
        sessionEventListenerGroup.addLink(new SocketSessionEventListenerWrapper(listener));
    }

    @Override
    public void addSessionIdleEventListener(SocketSessionIdleEventListener listener) {
        sessionIdleEventListenerGroup.addLink(new SocketSessionIdleEventListenerWrapper(listener));
    }

    @Override
    public SocketSessionEventListenerWrapper getSessionEventListenerLink() {
        return sessionEventListenerGroup.getRootLink();
    }

    @Override
    public SocketSessionIdleEventListenerWrapper getSessionIdleEventListenerLink() {
        return sessionIdleEventListenerGroup.getRootLink();
    }

    @Override
    public BeatFutureFactory getBeatFutureFactory() {
        return beatFutureFactory;
    }

    @Override
    public void setBeatFutureFactory(BeatFutureFactory beatFutureFactory) {
        this.beatFutureFactory = beatFutureFactory;
    }

    @Override
    public ProtocolEncoder getProtocolEncoder() {
        return protocolEncoder;
    }

    @Override
    public ProtocolDecoder getProtocolDecoder() {
        return protocolDecoder;
    }

    public AbstractSocketChannelContext(ServerConfiguration configuration) {
        super(configuration);
    }

    @Override
    protected void clearContext() {
        super.clearContext();
        createChannelIdsSequence();
    }

    private void createChannelIdsSequence() {
        int core_size = serverConfiguration.getSERVER_CORE_SIZE();
        int max = (Integer.MAX_VALUE / core_size) * core_size - 1;
        this.CHANNEL_ID = new FixedAtomicInteger(0, max);
    }

    @Override
    protected void doStart() throws Exception {

        if (ioEventHandleAdaptor == null) {
            throw new IllegalArgumentException("null ioEventHandle");
        }

        if (protocolFactory == null) {
            throw new IllegalArgumentException("null protocolFactory");
        }

        if (!initialized) {
            initialized = true;
            serverConfiguration.initializeDefault(this);
        }

        createChannelIdsSequence();

        EmptyFuture.initializeReadFuture(this);

        if (isEnableSSL()) {
            //			this.sslContext.initialize(this);
        }

        int SERVER_CORE_SIZE = serverConfiguration.getSERVER_CORE_SIZE();
        int server_port = serverConfiguration.getSERVER_PORT();
        long session_idle = serverConfiguration.getSERVER_SESSION_IDLE_TIME();
        String protocolId = protocolFactory.getProtocolId();

        this.encoding = serverConfiguration.getSERVER_ENCODING();
        this.sessionIdleTime = serverConfiguration.getSERVER_SESSION_IDLE_TIME();

        if (protocolEncoder == null) {
            this.protocolEncoder = protocolFactory.getProtocolEncoder();
            this.protocolDecoder = protocolFactory.getProtocolDecoder();
        }

        this.initializeByteBufAllocator();

        LoggerUtil.prettyLog(logger,
                "======================================= service begin to start =======================================");
        LoggerUtil.prettyLog(logger, "encoding              :{ {} }", encoding);
        LoggerUtil.prettyLog(logger, "protocol              :{ {} }", protocolId);
        LoggerUtil.prettyLog(logger, "cpu size              :{ cpu * {} }", SERVER_CORE_SIZE);
        LoggerUtil.prettyLog(logger, "enable ssl            :{ {} }", isEnableSSL());
        LoggerUtil.prettyLog(logger, "session idle          :{ {} }", session_idle);
        LoggerUtil.prettyLog(logger, "listen port(tcp)      :{ {} }", server_port);

        if (serverConfiguration.isSERVER_ENABLE_MEMORY_POOL()) {

            long SERVER_MEMORY_POOL_CAPACITY = serverConfiguration.getSERVER_MEMORY_POOL_CAPACITY()
                    * SERVER_CORE_SIZE;
            long SERVER_MEMORY_POOL_UNIT = serverConfiguration.getSERVER_MEMORY_POOL_UNIT();

            double MEMORY_POOL_SIZE = new BigDecimal(
                    SERVER_MEMORY_POOL_CAPACITY * SERVER_MEMORY_POOL_UNIT)
                            .divide(new BigDecimal(1024 * 1024), 2, BigDecimal.ROUND_HALF_UP)
                            .doubleValue();

            LoggerUtil.prettyLog(logger, "memory pool cap       :{ {} * {} ≈ {} M }", new Object[] {
                    SERVER_MEMORY_POOL_UNIT, SERVER_MEMORY_POOL_CAPACITY, MEMORY_POOL_SIZE });
        }

        ioEventHandleAdaptor.initialize(this);

        if (executorEventLoopGroup == null) {

            int eventLoopSize = serverConfiguration.getSERVER_CORE_SIZE();

            if (serverConfiguration.isSERVER_ENABLE_WORK_EVENT_LOOP()) {
                this.executorEventLoopGroup = new ThreadEventLoopGroup(this, "event-process",
                        eventLoopSize);
            } else {
                this.executorEventLoopGroup = new LineEventLoopGroup("event-process",
                        eventLoopSize);
            }
        }

        if (foreReadFutureAcceptor == null) {
            foreReadFutureAcceptor = new EventLoopFutureAcceptor();
        }

        foreReadFutureAcceptor.initialize(this);

        if (channelByteBufReaderGroup.getRootLink() == null) {

            channelByteBufReaderGroup.addLink(new IoLimitChannelByteBufReader());

            if (enableSSL) {
                channelByteBufReaderGroup.addLink(new SslChannelByteBufReader());
            }

            channelByteBufReaderGroup.addLink(new TransparentByteBufReader(this));
        }

        if (sessionFactory == null) {
            sessionFactory = new SocketSessionFactoryImpl();
        }

        LifeCycleUtil.start(byteBufAllocatorManager);

        LifeCycleUtil.start(executorEventLoopGroup);

        doStartModule();
    }

    protected void doStartModule() throws Exception {

    }

    protected void doStopModule() {

    }

    @Override
    protected void doStop() throws Exception {

        LifeCycleUtil.stop(executorEventLoopGroup);

        try {
            ioEventHandleAdaptor.destroy(this);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        LifeCycleUtil.stop(byteBufAllocatorManager);

        clearContext();

        doStopModule();
    }

    @Override
    public ProtocolFactory getProtocolFactory() {
        return protocolFactory;
    }

    @Override
    public IoEventHandleAdaptor getIoEventHandleAdaptor() {
        return ioEventHandleAdaptor;
    }

    @Override
    public ExecutorEventLoopGroup getExecutorEventLoopGroup() {
        return executorEventLoopGroup;
    }

    @Override
    public void setIoEventHandleAdaptor(IoEventHandleAdaptor ioEventHandleAdaptor) {
        this.ioEventHandleAdaptor = ioEventHandleAdaptor;
    }

    @Override
    public void setProtocolFactory(ProtocolFactory protocolFactory) {
        this.protocolFactory = protocolFactory;
    }

    @Override
    public void setExecutorEventLoopGroup(ExecutorEventLoopGroup executorEventLoopGroup) {
        this.executorEventLoopGroup = executorEventLoopGroup;
    }

    @Override
    public SslContext getSslContext() {
        return sslContext;
    }

    @Override
    public void setSslContext(SslContext sslContext) {
        if (sslContext == null) {
            throw new IllegalArgumentException("null sslContext");
        }
        this.sslContext = sslContext;
        this.enableSSL = true;
    }

    @Override
    public boolean isEnableSSL() {
        return enableSSL;
    }

    @Override
    public SocketSessionFactory getSessionFactory() {
        return sessionFactory;
    }

    @Override
    public void setSocketSessionFactory(SocketSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public ChannelByteBufReader getChannelByteBufReader() {
        return channelByteBufReaderGroup.getRootLink();
    }

    @Override
    public ForeFutureAcceptor getForeReadFutureAcceptor() {
        return foreReadFutureAcceptor;
    }

    /**
     * @return the CHANNEL_ID
     */
    public FixedAtomicInteger getCHANNEL_ID() {
        return CHANNEL_ID;
    }

}
