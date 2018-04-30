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
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.generallycloud.baseio.AbstractLifeCycle;
import com.generallycloud.baseio.LifeCycleUtil;
import com.generallycloud.baseio.buffer.ByteBufAllocatorManager;
import com.generallycloud.baseio.buffer.PooledByteBufAllocatorManager;
import com.generallycloud.baseio.buffer.UnpooledByteBufAllocatorManager;
import com.generallycloud.baseio.common.ClassUtil;
import com.generallycloud.baseio.common.LoggerUtil;
import com.generallycloud.baseio.component.ssl.SslContext;
import com.generallycloud.baseio.concurrent.ExecutorEventLoopGroup;
import com.generallycloud.baseio.configuration.ServerConfiguration;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.ProtocolCodec;

public abstract class AbstractSocketChannelContext extends AbstractLifeCycle
        implements SocketChannelContext {

    private Map<Object, Object>                   attributes  = new HashMap<>();
    private ByteBufAllocatorManager               byteBufAllocatorManager;
    private boolean                               enableSSL;
    private Charset                               encoding;
    private ExecutorEventLoopGroup                executorEventLoopGroup;
    private ForeFutureAcceptor                    foreReadFutureAcceptor;
    private boolean                               initialized;
    private IoEventHandleAdaptor                  ioEventHandleAdaptor;
    private Logger                                logger      = LoggerFactory.getLogger(getClass());
    private ProtocolCodec                         protocolCodec;
    private ServerConfiguration                   serverConfiguration;
    private SocketSessionELWrapper     sessionEventListenerRoot;
    private SocketSessionFactory                  sessionFactory;
    private SocketSessionIEListenerWrapper sessionIdleEventListenerRoot;
    private long                                  sessionIdleTime;
    private SslContext                            sslContext;
    private long                                  startupTime = System.currentTimeMillis();
    private SimulateSocketChannel                 simulateSocketChannel;

    public AbstractSocketChannelContext(ServerConfiguration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("null configuration");
        }
        this.serverConfiguration = configuration;
        this.addLifeCycleListener(new ChannelContextListener());
        this.sessionIdleTime = configuration.getSERVER_SESSION_IDLE_TIME();
    }

    @Override
    public void addSessionEventListener(SocketSessionEventListener listener) {
        if (sessionEventListenerRoot == null) {
            sessionEventListenerRoot = new SocketSessionELWrapper(listener);
        } else {
            ClassUtil.setValueOfLast(sessionEventListenerRoot,
                    new SocketSessionELWrapper(listener), "next");
        }
    }

    @Override
    public void addSessionIdleEventListener(SocketSessionIdleEventListener listener) {
        if (sessionIdleEventListenerRoot == null) {
            sessionIdleEventListenerRoot = new SocketSessionIEListenerWrapper(listener);
        } else {
            ClassUtil.setValueOfLast(sessionIdleEventListenerRoot,
                    new SocketSessionIEListenerWrapper(listener), "next");
        }
    }

    @Override
    public void clearAttributes() {
        this.attributes.clear();
    }

    protected void clearContext() {
        this.clearAttributes();
    }

    protected abstract ExecutorEventLoopGroup createExecutorEventLoopGroup();

    @Override
    protected void doStart() throws Exception {

        if (ioEventHandleAdaptor == null) {
            throw new IllegalArgumentException("null ioEventHandle");
        }

        if (protocolCodec == null) {
            throw new IllegalArgumentException("null protocolCodec");
        }

        if (!initialized) {
            initialized = true;
            serverConfiguration.initializeDefault(this);
        }

        int SERVER_CORE_SIZE = serverConfiguration.getSERVER_CORE_SIZE();
        int server_port = serverConfiguration.getSERVER_PORT();
        long session_idle = serverConfiguration.getSERVER_SESSION_IDLE_TIME();
        String protocolId = protocolCodec.getProtocolId();

        this.encoding = serverConfiguration.getSERVER_ENCODING();
        this.sessionIdleTime = serverConfiguration.getSERVER_SESSION_IDLE_TIME();

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

        protocolCodec.initialize(this);

        ioEventHandleAdaptor.initialize(this);

        if (executorEventLoopGroup == null) {
            this.executorEventLoopGroup = createExecutorEventLoopGroup();
        }

        if (foreReadFutureAcceptor == null) {
            foreReadFutureAcceptor = new EventLoopFutureAcceptor();
        }

        foreReadFutureAcceptor.initialize(this);

        if (sessionFactory == null) {
            sessionFactory = new SocketSessionFactoryImpl();
        }

        LifeCycleUtil.start(byteBufAllocatorManager);

        LifeCycleUtil.start(executorEventLoopGroup);

        doStartModule();
    }

    protected void doStartModule() throws Exception {
        this.simulateSocketChannel = new SimulateSocketChannel(this);
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

    protected void doStopModule() {

    }

    @Override
    public Object getAttribute(Object key) {
        return this.attributes.get(key);
    }

    @Override
    public Set<Object> getAttributeNames() {
        return this.attributes.keySet();
    }

    @Override
    public ByteBufAllocatorManager getByteBufAllocatorManager() {
        return byteBufAllocatorManager;
    }

    @Override
    public Charset getEncoding() {
        return encoding;
    }

    @Override
    public ExecutorEventLoopGroup getExecutorEventLoopGroup() {
        return executorEventLoopGroup;
    }

    @Override
    public ForeFutureAcceptor getForeReadFutureAcceptor() {
        return foreReadFutureAcceptor;
    }

    @Override
    public IoEventHandleAdaptor getIoEventHandleAdaptor() {
        return ioEventHandleAdaptor;
    }

    @Override
    public ProtocolCodec getProtocolCodec() {
        return protocolCodec;
    }

    @Override
    public ServerConfiguration getServerConfiguration() {
        return serverConfiguration;
    }

    @Override
    public SocketSessionELWrapper getSessionEventListenerLink() {
        return sessionEventListenerRoot;
    }

    @Override
    public SocketSessionFactory getSessionFactory() {
        return sessionFactory;
    }

    @Override
    public SocketSessionIEListenerWrapper getSessionIdleEventListenerLink() {
        return sessionIdleEventListenerRoot;
    }

    @Override
    public long getSessionIdleTime() {
        return sessionIdleTime;
    }

    @Override
    public SslContext getSslContext() {
        return sslContext;
    }

    @Override
    public long getStartupTime() {
        return startupTime;
    }

    protected void initializeByteBufAllocator() {
        if (getByteBufAllocatorManager() == null) {
            if (serverConfiguration.isSERVER_ENABLE_MEMORY_POOL()) {
                this.byteBufAllocatorManager = new PooledByteBufAllocatorManager(this);
            } else {
                this.byteBufAllocatorManager = new UnpooledByteBufAllocatorManager(this);
            }
        }
    }

    @Override
    public boolean isEnableSSL() {
        return enableSSL;
    }

    @Override
    public ChannelByteBufReader newChannelByteBufReader() {
        IoLimitChannelByteBufReader reader = new IoLimitChannelByteBufReader();
        if (enableSSL) {
            ClassUtil.setValueOfLast(reader, new SslChannelByteBufReader(), "next");
        }
        ClassUtil.setValueOfLast(reader, new TransparentByteBufReader(this), "next");
        return reader;
    }

    @Override
    public Object removeAttribute(Object key) {
        return this.attributes.remove(key);
    }

    @Override
    public void setAttribute(Object key, Object value) {
        this.attributes.put(key, value);
    }

    @Override
    public void setByteBufAllocatorManager(ByteBufAllocatorManager byteBufAllocatorManager) {
        this.byteBufAllocatorManager = byteBufAllocatorManager;
    }

    @Override
    public void setExecutorEventLoopGroup(ExecutorEventLoopGroup executorEventLoopGroup) {
        this.executorEventLoopGroup = executorEventLoopGroup;
    }

    @Override
    public void setIoEventHandleAdaptor(IoEventHandleAdaptor ioEventHandleAdaptor) {
        this.ioEventHandleAdaptor = ioEventHandleAdaptor;
    }

    @Override
    public void setProtocolCodec(ProtocolCodec protocolCodec) {
        this.protocolCodec = protocolCodec;
    }

    @Override
    public void setSocketSessionFactory(SocketSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public void setSslContext(SslContext sslContext) {
        if (sslContext == null) {
            throw new IllegalArgumentException("null sslContext");
        }
        this.sslContext = sslContext;
        this.enableSSL = true;
    }

    public SimulateSocketChannel getSimulateSocketChannel() {
        return simulateSocketChannel;
    }

}
