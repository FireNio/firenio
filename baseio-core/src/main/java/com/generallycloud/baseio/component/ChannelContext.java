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

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.generallycloud.baseio.AbstractLifeCycle;
import com.generallycloud.baseio.LifeCycleUtil;
import com.generallycloud.baseio.common.Assert;
import com.generallycloud.baseio.common.Encoding;
import com.generallycloud.baseio.common.FileUtil;
import com.generallycloud.baseio.common.LoggerUtil;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.ssl.SSLUtil;
import com.generallycloud.baseio.component.ssl.SslContext;
import com.generallycloud.baseio.concurrent.ExecutorEventLoopGroup;
import com.generallycloud.baseio.concurrent.LineEventLoopGroup;
import com.generallycloud.baseio.concurrent.ThreadEventLoopGroup;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.ProtocolCodec;

public class ChannelContext extends AbstractLifeCycle {

    private Map<Object, Object>            attributes         = new HashMap<>();
    private String                         certCrt;
    private String                         certKey;
    private ChannelManager                 channelManager     = new ChannelManager();
    private ChannelService                 channelService;
    private Charset                        charset            = Encoding.UTF8;
    private boolean                        enableHeartbeatLog = true;
    private boolean                        enableSsl;
    //是否启用work event loop，如果启用，则future在work event loop中处理
    private boolean                        enableWorkEventLoop;
    private ExecutorEventLoopGroup         executorEventLoopGroup;
    private HeartBeatLogger                heartBeatLogger;
    private String                         host               = "127.0.0.1";
    private boolean                        initialized;
    private IoEventHandle                  ioEventHandle      = DefaultIoEventHandle.get();
    private Logger                         logger             = LoggerFactory.getLogger(getClass());
    private int                            maxWriteBacklog    = 256;
    private NioEventLoopGroup              nioEventLoopGroup;
    private int                            port;
    private ProtocolCodec                  protocolCodec;
    private List<ChannelEventListener>     ssels              = new ArrayList<>();
    private List<ChannelIdleEventListener> ssiels             = new ArrayList<>();
    private SslContext                     sslContext;
    private String                         sslKeystore;
    private long                           startupTime        = System.currentTimeMillis();
    private int                            workEventQueueSize = 1024 * 256;

    public ChannelContext() {
        this(0);
    }

    public ChannelContext(int port) {
        this(null, port);
    }

    public ChannelContext(String host, int port) {
        this.port = port;
        this.host = host;
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
        Assert.notNull(ioEventHandle, "null ioEventHandleAdaptor");
        Assert.notNull(charset, "null charset");
        Assert.notNull(protocolCodec, "null protocolCodec");
        if (!initialized) {
            initialized = true;
        }
        initHeartBeatLogger();
        initSslContext(getClass().getClassLoader());
        String protocolId = protocolCodec.getProtocolId();
        int eventLoopSize = nioEventLoopGroup.getEventLoopSize();
        long channelIdle = nioEventLoopGroup.getIdleTime();
        LoggerUtil.prettyLog(logger, "charset               :{ {} }", charset);
        LoggerUtil.prettyLog(logger, "protocol              :{ {} }", protocolId);
        LoggerUtil.prettyLog(logger, "event loop size       :{ {} }", eventLoopSize);
        LoggerUtil.prettyLog(logger, "enable ssl            :{ {} }", isEnableSsl());
        LoggerUtil.prettyLog(logger, "channel idle          :{ {} }", channelIdle);
        LoggerUtil.prettyLog(logger, "listen port(tcp)      :{ {} }", port);
        if (nioEventLoopGroup.isEnableMemoryPool()) {
            long memoryPoolCapacity = nioEventLoopGroup.getMemoryPoolCapacity() * eventLoopSize;
            long memoryPoolUnit = nioEventLoopGroup.getMemoryPoolUnit();
            double memoryPoolSize = new BigDecimal(memoryPoolCapacity * memoryPoolUnit)
                    .divide(new BigDecimal(1024 * 1024), 2, BigDecimal.ROUND_HALF_UP).doubleValue();
            LoggerUtil.prettyLog(logger, "memory pool cap       :{ {} * {} ≈ {} M }",
                    new Object[] { memoryPoolUnit, memoryPoolCapacity, memoryPoolSize });
        }
        protocolCodec.initialize(this);
        if (executorEventLoopGroup == null) {
            if (isEnableWorkEventLoop()) {
                executorEventLoopGroup = new ThreadEventLoopGroup(this, "event-process",
                        eventLoopSize);
            } else {
                executorEventLoopGroup = new LineEventLoopGroup("event-process", eventLoopSize);
            }
        }
        LifeCycleUtil.start(executorEventLoopGroup);
    }

    @Override
    protected void doStop() throws Exception {
        LifeCycleUtil.stop(executorEventLoopGroup);
        this.attributes.clear();
    }

    public Object getAttribute(Object key) {
        return this.attributes.get(key);
    }

    public Set<Object> getAttributeNames() {
        return this.attributes.keySet();
    }

    public String getCertCrt() {
        return certCrt;
    }

    public String getCertKey() {
        return certKey;
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

    public ChannelService getChannelService() {
        return channelService;
    }

    public Charset getCharset() {
        return charset;
    }

    public ExecutorEventLoopGroup getExecutorEventLoopGroup() {
        return executorEventLoopGroup;
    }

    public HeartBeatLogger getHeartBeatLogger() {
        return heartBeatLogger;
    }

    public String getHost() {
        return host;
    }

    public IoEventHandle getIoEventHandle() {
        return ioEventHandle;
    }

    public int getMaxWriteBacklog() {
        return maxWriteBacklog;
    }

    public NioEventLoopGroup getNioEventLoopGroup() {
        return nioEventLoopGroup;
    }

    public int getPort() {
        return port;
    }

    public ProtocolCodec getProtocolCodec() {
        return protocolCodec;
    }

    public SslContext getSslContext() {
        return sslContext;
    }

    public String getSslKeystore() {
        return sslKeystore;
    }

    public long getStartupTime() {
        return startupTime;
    }

    public int getWorkEventQueueSize() {
        return workEventQueueSize;
    }

    private void initHeartBeatLogger() {
        final Logger logger = LoggerFactory.getLogger("hb");
        if (isEnableHeartbeatLog()) {
            heartBeatLogger = new HeartBeatLogger() {
                @Override
                public void logPing(NioSocketChannel channel) {
                    logger.info("hb req from: {}", channel);
                }

                @Override
                public void logPong(NioSocketChannel channel) {
                    logger.info("hb res from: {}", channel);
                }
            };
        } else {
            heartBeatLogger = new HeartBeatLogger() {
                @Override
                public void logPing(NioSocketChannel channel) {
                    logger.debug("hb req from: {}", channel);
                }

                @Override
                public void logPong(NioSocketChannel channel) {
                    logger.debug("hb res from: {}", channel);
                }
            };
        }
    }

    private void initSslContext(ClassLoader classLoader) throws IOException {
        if (isEnableSsl() && getSslContext() == null) {
            if (!StringUtil.isNullOrBlank(getCertCrt())) {
                File certificate = FileUtil.readFileByCls(getCertCrt(), classLoader);
                File privateKey = FileUtil.readFileByCls(getCertKey(), classLoader);
                SslContext sslContext = SSLUtil.initServer(privateKey, certificate);
                setSslContext(sslContext);
                return;
            }
            if (!StringUtil.isNullOrBlank(getSslKeystore())) {
                String keystoreInfo = getSslKeystore();
                String[] params = keystoreInfo.split(";");
                if (params.length != 4) {
                    throw new IllegalArgumentException("SERVER_SSL_KEYSTORE config error");
                }
                File storeFile = FileUtil.readFileByCls(params[0], classLoader);
                SslContext sslContext = SSLUtil.initServer(storeFile, params[1], params[2],
                        params[3]);
                setSslContext(sslContext);
                return;
            }
            throw new IllegalArgumentException("ssl enabled,but there is no config for");
        }
    }

    public boolean isEnableHeartbeatLog() {
        return enableHeartbeatLog;
    }

    public boolean isEnableSsl() {
        return enableSsl;
    }

    public boolean isEnableWorkEventLoop() {
        return enableWorkEventLoop;
    }

    public Object removeAttribute(Object key) {
        return this.attributes.remove(key);
    }

    public void setAttribute(Object key, Object value) {
        this.attributes.put(key, value);
    }

    public void setCertCrt(String certCrt) {
        checkNotRunning();
        this.certCrt = certCrt;
    }

    public void setCertKey(String certKey) {
        checkNotRunning();
        this.certKey = certKey;
    }

    public void setChannelService(ChannelService service) {
        checkNotRunning();
        this.channelService = service;
    }

    public void setCharset(Charset charset) {
        checkNotRunning();
        this.charset = charset;
    }

    public void setEnableHeartbeatLog(boolean enableHeartbeatLog) {
        checkNotRunning();
        this.enableHeartbeatLog = enableHeartbeatLog;
    }

    public void setEnableSsl(boolean enableSsl) {
        checkNotRunning();
        this.enableSsl = enableSsl;
    }

    public void setEnableWorkEventLoop(boolean enableWorkEventLoop) {
        checkNotRunning();
        this.enableWorkEventLoop = enableWorkEventLoop;
    }

    public void setExecutorEventLoopGroup(ExecutorEventLoopGroup executorEventLoopGroup) {
        checkNotRunning();
        this.executorEventLoopGroup = executorEventLoopGroup;
    }

    public void setHost(String host) {
        checkNotRunning();
        this.host = host;
    }

    public void setIoEventHandle(IoEventHandle ioEventHandle) {
        checkNotRunning();
        this.ioEventHandle = ioEventHandle;
    }

    public void setMaxWriteBacklog(int maxWriteBacklog) {
        checkNotRunning();
        this.maxWriteBacklog = maxWriteBacklog;
    }

    public void setNioEventLoopGroup(NioEventLoopGroup nioEventLoopGroup) {
        checkNotRunning();
        this.nioEventLoopGroup = nioEventLoopGroup;
    }

    public void setPort(int port) {
        checkNotRunning();
        this.port = port;
    }

    public void setProtocolCodec(ProtocolCodec protocolCodec) {
        checkNotRunning();
        this.protocolCodec = protocolCodec;
    }

    public void setSslContext(SslContext sslContext) {
        checkNotRunning();
        if (sslContext == null) {
            throw new IllegalArgumentException("null sslContext");
        }
        this.sslContext = sslContext;
        this.enableSsl = true;
    }

    public void setSslKeystore(String sslKeystore) {
        checkNotRunning();
        this.sslKeystore = sslKeystore;
    }

    public void setWorkEventQueueSize(int workEventQueueSize) {
        checkNotRunning();
        this.workEventQueueSize = workEventQueueSize;
    }

    public interface HeartBeatLogger {

        void logPing(NioSocketChannel channel);

        void logPong(NioSocketChannel channel);
    }

}
