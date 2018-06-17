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

    private Map<Object, Object>            attributes    = new HashMap<>();
    private ChannelService                 channelService;
    private Configuration                  configuration;
    private boolean                        enableSsl;
    private Charset                        encoding;
    private ExecutorEventLoopGroup         executorEventLoopGroup;
    private boolean                        initialized;
    private IoEventHandleAdaptor           ioEventHandle = new DefaultIoEventHandle();
    private Logger                         logger        = LoggerFactory.getLogger(getClass());
    private ProtocolCodec                  protocolCodec;
    private NioEventLoopGroup              nioEventLoopGroup;
    private ChannelManager                 channelManager;
    private List<ChannelEventListener>     ssels         = new ArrayList<>();
    private List<ChannelIdleEventListener> ssiels        = new ArrayList<>();
    private SslContext                     sslContext;
    private NioSocketChannel               simulateSocketChannel;
    private boolean   enableWorkEventLoop;
    private long                           startupTime   = System.currentTimeMillis();

    public ChannelContext(Configuration configuration) {
        this.configuration = configuration;
        this.addLifeCycleListener(new ChannelContextListener());
    }

    public void addChannelEventListener(ChannelEventListener listener) {
        checkNotRunning();
        ssels.add(listener);
    }

    public void addChannelIdleEventListener(ChannelIdleEventListener listener) {
        checkNotRunning();
        ssiels.add(listener);
    }

    private void checkNotRunning() {
        if (isRunning()) {
            throw new UnsupportedOperationException("starting or running");
        }
    }

    @Override
    protected void doStart() throws Exception {
        Assert.notNull(configuration, "null configuration");
        Assert.notNull(ioEventHandle, "null ioEventHandleAdaptor");
        Assert.notNull(protocolCodec, "null protocolCodec");
        if (!initialized) {
            initialized = true;
        }
        String protocolId = protocolCodec.getProtocolId();
        int eventLoopSize = nioEventLoopGroup.getEventLoopSize();
        int serverPort = configuration.getPort();
        long channelIdle = nioEventLoopGroup.getIdleTime();
        this.encoding = configuration.getCharset();
        this.enableWorkEventLoop = configuration.isEnableWorkEventLoop();
        LoggerUtil.prettyLog(logger, "encoding              :{ {} }", encoding);
        LoggerUtil.prettyLog(logger, "protocol              :{ {} }", protocolId);
        LoggerUtil.prettyLog(logger, "event loop size       :{ {} }", eventLoopSize);
        LoggerUtil.prettyLog(logger, "enable ssl            :{ {} }", isEnableSsl());
        LoggerUtil.prettyLog(logger, "channel idle          :{ {} }", channelIdle);
        LoggerUtil.prettyLog(logger, "listen port(tcp)      :{ {} }", serverPort);
        if (nioEventLoopGroup.isEnableMemoryPool()) {
            long memoryPoolCapacity = nioEventLoopGroup.getMemoryPoolCapacity() * eventLoopSize;
            long memoryPoolUnit = nioEventLoopGroup.getMemoryPoolUnit();
            double memoryPoolSize = new BigDecimal(memoryPoolCapacity * memoryPoolUnit)
                    .divide(new BigDecimal(1024 * 1024), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
            LoggerUtil.prettyLog(logger, "memory pool cap       :{ {} * {} ≈ {} M }",
                    new Object[] { memoryPoolUnit, memoryPoolCapacity, memoryPoolSize });
        }
        createHeartBeatLogger();
        channelManager = new ChannelManager(this);
        protocolCodec.initialize(this);
        ioEventHandle.initialize(this);
        if (executorEventLoopGroup == null) {
            if (getConfiguration().isEnableWorkEventLoop()) {
                executorEventLoopGroup = new ThreadEventLoopGroup(this, "event-process",
                        eventLoopSize);
            } else {
                executorEventLoopGroup = new LineEventLoopGroup("event-process", eventLoopSize);
            }
        }
        LifeCycleUtil.start(executorEventLoopGroup);
        this.simulateSocketChannel = new NioSocketChannel(this,
                UnpooledByteBufAllocator.getDirect());
    }

    @Override
    protected void doStop() throws Exception {
        LifeCycleUtil.stop(executorEventLoopGroup);
        try {
            ioEventHandle.destroy(this);
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

    public IoEventHandle getIoEventHandle() {
        return ioEventHandle;
    }

    public ProtocolCodec getProtocolCodec() {
        return protocolCodec;
    }

    public NioEventLoopGroup getNioEventLoopGroup() {
        return nioEventLoopGroup;
    }

    public List<ChannelEventListener> getChannelEventListeners() {
        return ssels;
    }

    public List<ChannelIdleEventListener> getChannelIdleEventListeners() {
        return ssiels;
    }

    public ChannelManager getChannelManager() {
        return channelManager;
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

    public void setIoEventHandle(IoEventHandleAdaptor ioEventHandleAdaptor) {
        checkNotRunning();
        this.ioEventHandle = ioEventHandleAdaptor;
    }

    public void setProtocolCodec(ProtocolCodec protocolCodec) {
        checkNotRunning();
        this.protocolCodec = protocolCodec;
    }

    public void setNioEventLoopGroup(NioEventLoopGroup nioEventLoopGroup) {
        this.nioEventLoopGroup = nioEventLoopGroup;
    }

    public void setSslContext(SslContext sslContext) {
        checkNotRunning();
        if (sslContext == null) {
            throw new IllegalArgumentException("null sslContext");
        }
        this.sslContext = sslContext;
        this.enableSsl = true;
    }
    
    public boolean isEnableWorkEventLoop() {
        return enableWorkEventLoop;
    }

    public NioSocketChannel getSimulateSocketChannel() {
        return simulateSocketChannel;
    }
    
    private HeartBeatLogger heartBeatLogger;
    
    public HeartBeatLogger getHeartBeatLogger() {
        return heartBeatLogger;
    }
    
    private void createHeartBeatLogger() {
        if (getConfiguration().isEnableHeartbeatLog()) {
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
    
    public interface HeartBeatLogger {

        void logRequest(NioSocketChannel channel);

        void logResponse(NioSocketChannel channel);
    }

}
