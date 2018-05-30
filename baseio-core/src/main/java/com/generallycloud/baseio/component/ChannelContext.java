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
import com.generallycloud.baseio.buffer.UnpooledByteBufAllocator;
import com.generallycloud.baseio.common.Assert;
import com.generallycloud.baseio.common.LoggerUtil;
import com.generallycloud.baseio.component.ssl.SslContext;
import com.generallycloud.baseio.concurrent.ExecutorEventLoopGroup;
import com.generallycloud.baseio.concurrent.LineEventLoopGroup;
import com.generallycloud.baseio.concurrent.ThreadEventLoopGroup;
import com.generallycloud.baseio.configuration.Configuration;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.ProtocolCodec;

public class ChannelContext extends AbstractLifeCycle {

    private Map<Object, Object>                  attributes  = new HashMap<>();
    private ChannelService                       channelService;
    private Configuration                        configuration;
    private boolean                              enableSsl;
    private Charset                              encoding;
    private ExecutorEventLoopGroup               executorEventLoopGroup;
    private ForeFutureAcceptor                   foreFutureAcceptor;
    private boolean                              initialized;
    private IoEventHandleAdaptor                 ioEventHandleAdaptor;
    private Logger                               logger      = LoggerFactory.getLogger(getClass());
    private ProtocolCodec                        protocolCodec;
    private SelectorEventLoopGroup               selectorEventLoopGroup;
    private SocketSessionFactory                 sessionFactory;
    private SocketSessionManager                 sessionManager;
    private List<SocketSessionEventListener>     ssels       = new ArrayList<>();
    private List<SocketSessionIdleEventListener> ssiels      = new ArrayList<>();
    private SslContext                           sslContext;
    private SimulateSocketChannel                simulateSocketChannel;
    private long                                 startupTime = System.currentTimeMillis();

    public ChannelContext(Configuration configuration) {
        this.configuration = configuration;
        this.addLifeCycleListener(new ChannelContextListener());
    }

    public void addSessionEventListener(SocketSessionEventListener listener) {
        checkNotRunning();
        ssels.add(listener);
    }

    public void addSessionIdleEventListener(SocketSessionIdleEventListener listener) {
        checkNotRunning();
        ssiels.add(listener);
    }

    private void checkNotRunning() {
        if (isRunning()) {
            throw new UnsupportedOperationException("starting or running");
        }
    }

    protected ExecutorEventLoopGroup createExecutorEventLoopGroup() {
        int eventLoopSize = selectorEventLoopGroup.getEventLoopSize();
        if (getConfiguration().isEnableWorkEventLoop()) {
            return new ThreadEventLoopGroup(this, "event-process", eventLoopSize);
        } else {
            return new LineEventLoopGroup("event-process", eventLoopSize);
        }
    }

    @Override
    protected void doStart() throws Exception {
        Assert.notNull(configuration, "null configuration");
        Assert.notNull(ioEventHandleAdaptor, "null ioEventHandleAdaptor");
        Assert.notNull(protocolCodec, "null protocolCodec");
        if (!initialized) {
            initialized = true;
        }
        String protocolId = protocolCodec.getProtocolId();
        int eventLoopSize = selectorEventLoopGroup.getEventLoopSize();
        int serverPort = configuration.getPort();
        long sessionIdle = selectorEventLoopGroup.getIdleTime();
        this.encoding = configuration.getCharset();
        //        LoggerUtil.prettyLog(logger,
        //                "======================================= service begin to start =======================================");
        LoggerUtil.prettyLog(logger, "encoding              :{ {} }", encoding);
        LoggerUtil.prettyLog(logger, "protocol              :{ {} }", protocolId);
        LoggerUtil.prettyLog(logger, "event loop size       :{ {} }", eventLoopSize);
        LoggerUtil.prettyLog(logger, "enable ssl            :{ {} }", isEnableSsl());
        LoggerUtil.prettyLog(logger, "session idle          :{ {} }", sessionIdle);
        LoggerUtil.prettyLog(logger, "listen port(tcp)      :{ {} }", serverPort);
        if (selectorEventLoopGroup.isEnableMemoryPool()) {
            long memoryPoolCapacity = selectorEventLoopGroup.getMemoryPoolCapacity()
                    * eventLoopSize;
            long memoryPoolUnit = selectorEventLoopGroup.getMemoryPoolUnit();
            double memoryPoolSize = new BigDecimal(memoryPoolCapacity * memoryPoolUnit)
                    .divide(new BigDecimal(1024 * 1024), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
            LoggerUtil.prettyLog(logger, "memory pool cap       :{ {} * {} ≈ {} M }",
                    new Object[] { memoryPoolUnit, memoryPoolCapacity, memoryPoolSize });
        }
        sessionManager = new SocketSessionManager(this);
        protocolCodec.initialize(this);
        ioEventHandleAdaptor.initialize(this);
        if (executorEventLoopGroup == null) {
            this.executorEventLoopGroup = createExecutorEventLoopGroup();
        }
        if (foreFutureAcceptor == null) {
            if (getConfiguration().isEnableWorkEventLoop()) {
                foreFutureAcceptor = new EventLoopFutureAcceptor();
            } else {
                foreFutureAcceptor = new IoProcessFutureAcceptor();
            }
            foreFutureAcceptor.initialize(this);
        }
        if (sessionFactory == null) {
            sessionFactory = new SocketSessionFactoryImpl();
        }
        LifeCycleUtil.start(executorEventLoopGroup);
        this.simulateSocketChannel = new SimulateSocketChannel(this,
                UnpooledByteBufAllocator.getDirect());
    }

    @Override
    protected void doStop() throws Exception {
        LifeCycleUtil.stop(executorEventLoopGroup);
        try {
            ioEventHandleAdaptor.destroy(this);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        this.attributes.clear();
    }

    public Object getAttribute(Object key) {
        return this.attributes.get(key);
    }

    public Set<Object> getAttributeNames() {
        return this.attributes.keySet();
    }

    public ChannelService getChannelService() {
        return channelService;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public Charset getEncoding() {
        return encoding;
    }

    public ExecutorEventLoopGroup getExecutorEventLoopGroup() {
        return executorEventLoopGroup;
    }

    public ForeFutureAcceptor getForeFutureAcceptor() {
        return foreFutureAcceptor;
    }

    public IoEventHandleAdaptor getIoEventHandleAdaptor() {
        return ioEventHandleAdaptor;
    }

    public ProtocolCodec getProtocolCodec() {
        return protocolCodec;
    }

    public SelectorEventLoopGroup getSelectorEventLoopGroup() {
        return selectorEventLoopGroup;
    }

    public List<SocketSessionEventListener> getSessionEventListeners() {
        return ssels;
    }

    public SocketSessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public List<SocketSessionIdleEventListener> getSessionIdleEventListeners() {
        return ssiels;
    }

    public SocketSessionManager getSessionManager() {
        return sessionManager;
    }

    public SslContext getSslContext() {
        return sslContext;
    }

    public long getStartupTime() {
        return startupTime;
    }

    public boolean isEnableSsl() {
        return enableSsl;
    }

    public Object removeAttribute(Object key) {
        return this.attributes.remove(key);
    }

    public void setAttribute(Object key, Object value) {
        this.attributes.put(key, value);
    }

    public void setChannelService(ChannelService service) {
        this.channelService = service;
    }

    public void setExecutorEventLoopGroup(ExecutorEventLoopGroup executorEventLoopGroup) {
        checkNotRunning();
        this.executorEventLoopGroup = executorEventLoopGroup;
    }

    public void setIoEventHandleAdaptor(IoEventHandleAdaptor ioEventHandleAdaptor) {
        checkNotRunning();
        this.ioEventHandleAdaptor = ioEventHandleAdaptor;
    }

    public void setProtocolCodec(ProtocolCodec protocolCodec) {
        checkNotRunning();
        this.protocolCodec = protocolCodec;
    }

    public void setSelectorEventLoopGroup(SelectorEventLoopGroup selectorEventLoopGroup) {
        this.selectorEventLoopGroup = selectorEventLoopGroup;
    }

    public void setSocketSessionFactory(SocketSessionFactory sessionFactory) {
        checkNotRunning();
        this.sessionFactory = sessionFactory;
    }

    public void setSslContext(SslContext sslContext) {
        checkNotRunning();
        if (sslContext == null) {
            throw new IllegalArgumentException("null sslContext");
        }
        this.sslContext = sslContext;
        this.enableSsl = true;
    }

    public SimulateSocketChannel getSimulateSocketChannel() {
        return simulateSocketChannel;
    }

}
