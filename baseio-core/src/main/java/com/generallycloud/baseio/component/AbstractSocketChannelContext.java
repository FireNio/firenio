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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.generallycloud.baseio.AbstractLifeCycle;
import com.generallycloud.baseio.LifeCycleUtil;
import com.generallycloud.baseio.buffer.ByteBufAllocatorManager;
import com.generallycloud.baseio.buffer.PooledByteBufAllocatorManager;
import com.generallycloud.baseio.buffer.UnpooledByteBufAllocatorManager;
import com.generallycloud.baseio.common.LoggerUtil;
import com.generallycloud.baseio.component.ssl.SslContext;
import com.generallycloud.baseio.concurrent.ExecutorEventLoopGroup;
import com.generallycloud.baseio.configuration.Configuration;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.ProtocolCodec;

public abstract class AbstractSocketChannelContext extends AbstractLifeCycle
        implements SocketChannelContext {

    private Map<Object, Object>                  attributes  = new HashMap<>();
    private ByteBufAllocatorManager              byteBufAllocatorManager;
    private boolean                              enableSsl;
    private Charset                              encoding;
    private ExecutorEventLoopGroup               executorEventLoopGroup;
    private ForeFutureAcceptor                   foreFutureAcceptor;
    private boolean                              initialized;
    private IoEventHandleAdaptor                 ioEventHandleAdaptor;
    private Logger                               logger      = LoggerFactory.getLogger(getClass());
    private ProtocolCodec                        protocolCodec;
    private Configuration                        configuration;
    private SocketSessionFactory                 sessionFactory;
    private long                                 sessionIdleTime;
    private SimulateSocketChannel                simulateSocketChannel;
    private List<SocketSessionEventListener>     ssels       = new ArrayList<>();
    private List<SocketSessionIdleEventListener> ssiels      = new ArrayList<>();
    private SslContext                           sslContext;
    private long                                 startupTime = System.currentTimeMillis();

    public AbstractSocketChannelContext(Configuration configuration) {
        if (configuration == null) {
            throw new IllegalArgumentException("null configuration");
        }
        this.configuration = configuration;
        this.addLifeCycleListener(new ChannelContextListener());
        this.sessionIdleTime = configuration.getSessionIdleTime();
    }

    @Override
    public void addSessionEventListener(SocketSessionEventListener listener) {
        checkNotRunning();
        ssels.add(listener);
    }

    @Override
    public void addSessionIdleEventListener(SocketSessionIdleEventListener listener) {
        checkNotRunning();
        ssiels.add(listener);
    }

    private void checkNotRunning(){
        if (isRunning()) {
            throw new UnsupportedOperationException("starting or running");
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
            configuration.initializeDefault(this);
        }
        String protocolId = protocolCodec.getProtocolId();
        int coreSize = configuration.getCoreSize();
        int serverPort = configuration.getPort();
        long sessionIdle = configuration.getSessionIdleTime();
        this.encoding = configuration.getCharset();
        this.sessionIdleTime = configuration.getSessionIdleTime();
        this.initializeByteBufAllocator();
        LoggerUtil.prettyLog(logger,
                "======================================= service begin to start =======================================");
        LoggerUtil.prettyLog(logger, "encoding              :{ {} }", encoding);
        LoggerUtil.prettyLog(logger, "protocol              :{ {} }", protocolId);
        LoggerUtil.prettyLog(logger, "cpu size              :{ cpu * {} }", coreSize);
        LoggerUtil.prettyLog(logger, "enable ssl            :{ {} }", isEnableSsl());
        LoggerUtil.prettyLog(logger, "session idle          :{ {} }", sessionIdle);
        LoggerUtil.prettyLog(logger, "listen port(tcp)      :{ {} }", serverPort);
        if (configuration.isEnableMemoryPool()) {
            long memoryPoolCapacity = configuration.getMemoryPoolCapacity() * coreSize;
            long memoryPoolUnit = configuration.getMemoryPoolUnit();
            double memoryPoolSize = new BigDecimal( memoryPoolCapacity * memoryPoolUnit)
                            .divide(new BigDecimal(1024 * 1024), 2, BigDecimal.ROUND_HALF_UP)
                            .doubleValue();
            LoggerUtil.prettyLog(logger, "memory pool cap       :{ {} * {} ≈ {} M }", new Object[] {
                    memoryPoolUnit, memoryPoolCapacity, memoryPoolSize });
        }
        protocolCodec.initialize(this);
        ioEventHandleAdaptor.initialize(this);
        if (executorEventLoopGroup == null) {
            this.executorEventLoopGroup = createExecutorEventLoopGroup();
        }
        if (foreFutureAcceptor == null) {
            foreFutureAcceptor = new EventLoopFutureAcceptor();
        }
        foreFutureAcceptor.initialize(this);
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

    protected void doStopModule() {}

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
    public ForeFutureAcceptor getForeFutureAcceptor() {
        return foreFutureAcceptor;
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
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public List<SocketSessionEventListener> getSessionEventListeners() {
        return ssels;
    }
    
    @Override
    public SocketSessionFactory getSessionFactory() {
        return sessionFactory;
    }

    @Override
    public List<SocketSessionIdleEventListener> getSessionIdleEventListeners() {
        return ssiels;
    }

    @Override
    public long getSessionIdleTime() {
        return sessionIdleTime;
    }

    public SimulateSocketChannel getSimulateSocketChannel() {
        return simulateSocketChannel;
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
            if (configuration.isEnableMemoryPool()) {
                this.byteBufAllocatorManager = new PooledByteBufAllocatorManager(this);
            } else {
                this.byteBufAllocatorManager = new UnpooledByteBufAllocatorManager(this);
            }
        }
    }

    @Override
    public boolean isEnableSsl() {
        return enableSsl;
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
        checkNotRunning();
        this.byteBufAllocatorManager = byteBufAllocatorManager;
    }

    @Override
    public void setExecutorEventLoopGroup(ExecutorEventLoopGroup executorEventLoopGroup) {
        checkNotRunning();
        this.executorEventLoopGroup = executorEventLoopGroup;
    }

    @Override
    public void setIoEventHandleAdaptor(IoEventHandleAdaptor ioEventHandleAdaptor) {
        checkNotRunning();
        this.ioEventHandleAdaptor = ioEventHandleAdaptor;
    }

    @Override
    public void setProtocolCodec(ProtocolCodec protocolCodec) {
        checkNotRunning();
        this.protocolCodec = protocolCodec;
    }

    @Override
    public void setSocketSessionFactory(SocketSessionFactory sessionFactory) {
        checkNotRunning();
        this.sessionFactory = sessionFactory;
    }
    
    @Override
    public void setSslContext(SslContext sslContext) {
        checkNotRunning();
        if (sslContext == null) {
            throw new IllegalArgumentException("null sslContext");
        }
        this.sslContext = sslContext;
        this.enableSsl = true;
    }

}
