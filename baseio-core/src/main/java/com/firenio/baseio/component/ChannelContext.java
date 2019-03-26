/*
 * Copyright 2015 The Baseio Project
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
package com.firenio.baseio.component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.firenio.baseio.LifeCycle;
import com.firenio.baseio.Options;
import com.firenio.baseio.common.Assert;
import com.firenio.baseio.common.FileUtil;
import com.firenio.baseio.common.Properties;
import com.firenio.baseio.common.Util;
import com.firenio.baseio.concurrent.EventLoop;
import com.firenio.baseio.concurrent.EventLoopGroup;
import com.firenio.baseio.log.Logger;
import com.firenio.baseio.log.LoggerFactory;

public abstract class ChannelContext extends LifeCycle implements Configuration {

    private String[]                       applicationProtocols;
    private Map<Object, Object>            attributes         = new HashMap<>();
    private List<ChannelEventListener>     cels               = new ArrayList<>();
    private ChannelManager                 channelManager     = new ChannelManager();
    private Charset                        charset            = Util.UTF8;
    private List<ChannelIdleListener> ciels              = new ArrayList<>();
    private Map<String, ProtocolCodec>     codecs             = new HashMap<>();
    private ProtocolCodec                  defaultCodec;
    private boolean                        enableHeartbeatLog = true;
    private boolean                        enableSsl;
    //是否启用work event loop，如果启用，则frame在work event loop中处理
    private EventLoopGroup                 executorEventLoopGroup;
    private HeartBeatLogger                heartBeatLogger;
    private String                         host;
    private boolean                        initialized;
    private IoEventHandle                  ioEventHandle      = DefaultIoEventHandle.get();
    private Logger                         logger             = LoggerFactory.getLogger(getClass());
    private int                            maxWriteBacklog    = Integer.MAX_VALUE;
    private String                         openSslPath;
    private int                            port;
    private boolean                        printConfig        = true;
    private NioEventLoopGroup              processorGroup;
    private Properties                     properties;
    private InetSocketAddress              serverAddress;
    private SslContext                     sslContext;
    private String                         sslKeystore;
    private String                         sslPem;
    private long                           startupTime        = System.currentTimeMillis();

    ChannelContext(NioEventLoopGroup group, String host, int port) {
        Assert.notNull(host, "null host");
        Assert.notNull(group, "null group");
        this.port = port;
        this.host = host;
        this.processorGroup = group;
    }

    public void addChannelEventListener(ChannelEventListener listener) {
        checkNotRunning();
        cels.add(listener);
    }

    public void addChannelIdleEventListener(ChannelIdleListener listener) {
        checkNotRunning();
        ciels.add(listener);
    }

    public void addProtocolCodec(ProtocolCodec codec) {
        checkNotRunning();
        if (defaultCodec == null) {
            defaultCodec = codec;
        }
        codecs.put(codec.getProtocolId(), codec);
    }

    protected void channelEstablish(Channel ch, Throwable ex) {}

    @Override
    public void configurationChanged(Properties properties) {
        if (!Util.isNullOrBlank(openSslPath)) {
            Options.setOpensslPath(openSslPath);
        }
        this.properties = properties;
    }

    @Override
    protected void doStart() throws Exception {
        Assert.notNull(ioEventHandle, "null ioEventHandle");
        Assert.notNull(charset, "null charset");
        Assert.notNull(defaultCodec, "null protocolCodec");
        if (!initialized) {
            initialized = true;
        }
        initHeartBeatLogger();
        initSslContext(getClass().getClassLoader());
        NioEventLoopGroup g = this.processorGroup;
        int eventLoopSize = g.getEventLoopSize();
        if (Util.isNullOrBlank(host)) {
            this.serverAddress = new InetSocketAddress(port);
        } else {
            this.serverAddress = new InetSocketAddress(host, port);
        }
        Util.start(executorEventLoopGroup);
        Util.start(processorGroup);
        if (printConfig) {
            StringBuilder sb = new StringBuilder();
            for (String codecId : codecs.keySet()) {
                sb.append(codecId);
                sb.append(',');
                sb.append(' ');
            }
            sb.setLength(sb.length() - 2);
            logger.info("charset               : [ {} ]", charset);
            logger.info("protocol              : [ {} ]", sb.toString());
            logger.info("event loop size       : [ {} ]", eventLoopSize);
            logger.info("enable ssl            : [ {} ]", sslType());
            logger.info("enable epoll          : [ {} ]", Native.EPOLL_AVAIABLE);
            logger.info("channel idle          : [ {} ]", g.getIdleTime());
            logger.info("host and port         : [ {}:{} ]", getHost(), port);
            if (g.isEnableMemoryPool()) {
                long memoryPoolCapacity = g.getMemoryPoolCapacity() * g.getEventLoopSize();
                long memoryPoolByteSize = memoryPoolCapacity * g.getMemoryPoolUnit();
                double memoryPoolSize = memoryPoolByteSize / (1024 * 1024);
                logger.info("memory pool           : [ {}/{}/{}M ({}) ]", g.getMemoryPoolUnit(),
                        memoryPoolCapacity,
                        BigDecimal.valueOf(memoryPoolSize).setScale(2, BigDecimal.ROUND_HALF_UP),
                        getByteBufPoolType(g));
            }
            if (isEnableSsl()) {
                sb.setLength(0);
                for (String p : SslContext.ENABLED_PROTOCOLS) {
                    sb.append(p);
                    sb.append(',');
                    sb.append(' ');
                }
                sb.setLength(sb.length() - 2);
                logger.info("ssl default protocols : [ {} ]", sb.toString());
            }
        }
    }
    
    private String getByteBufPoolType(NioEventLoopGroup g){
        if (Options.isEnableUnsafeBuf()) {
            return "unsafe";
        }
        return g.isEnableMemoryPoolDirect() ? "direct" : "heap";
    }

    @Override
    protected void doStop() {
        for (Channel ch : channelManager.getManagedChannels().values()) {
            Util.close(ch);
        }
        stopEventLoopGroup(getProcessorGroup());
        Util.stop(executorEventLoopGroup);
        this.attributes.clear();
    }

    public String[] getApplicationProtocols() {
        return applicationProtocols;
    }

    public Object getAttribute(Object key) {
        return this.attributes.get(key);
    }

    public Set<Object> getAttributeNames() {
        return this.attributes.keySet();
    }

    public List<ChannelEventListener> getChannelEventListeners() {
        return cels;
    }

    public List<ChannelIdleListener> getChannelIdleEventListeners() {
        return ciels;
    }

    public ChannelManager getChannelManager() {
        return channelManager;
    }

    public Charset getCharset() {
        return charset;
    }

    public ProtocolCodec getDefaultCodec() {
        return defaultCodec;
    }

    public EventLoopGroup getExecutorEventLoopGroup() {
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

    public EventLoop getNextExecutorEventLoop() {
        if (executorEventLoopGroup == null) {
            return null;
        }
        return executorEventLoopGroup.getNext();
    }

    public String getOpenSslPath() {
        return openSslPath;
    }

    public int getPort() {
        return port;
    }

    public NioEventLoopGroup getProcessorGroup() {
        return processorGroup;
    }

    public Properties getProperties() {
        return properties;
    }

    public ProtocolCodec getProtocolCodec(String id) throws IOException {
        ProtocolCodec codec = codecs.get(id);
        if (codec == null) {
            throw new IOException("codec not found: " + id);
        }
        return codec;
    }

    InetSocketAddress getServerAddress() {
        return serverAddress;
    }

    public SslContext getSslContext() {
        return sslContext;
    }

    public String getSslKeystore() {
        return sslKeystore;
    }

    public String getSslPem() {
        return sslPem;
    }

    public long getStartupTime() {
        return startupTime;
    }

    private void initHeartBeatLogger() {
        if (isEnableHeartbeatLog()) {
            heartBeatLogger = new HeartBeatLogger() {
                final Logger logger = LoggerFactory.getLogger("hb");

                @Override
                public void logPing(Channel ch) {
                    logger.info("heart beat req from: {}", ch);
                }

                @Override
                public void logPong(Channel ch) {
                    logger.info("heart beat res from: {}", ch);
                }
            };
        } else {
            heartBeatLogger = new HeartBeatLogger() {
                final Logger logger = LoggerFactory.getLogger("hb");

                @Override
                public void logPing(Channel ch) {
                    logger.debug("hb req from: {}", ch);
                }

                @Override
                public void logPong(Channel ch) {
                    logger.debug("hb res from: {}", ch);
                }
            };
        }
    }

    private void initSslContext(ClassLoader classLoader) throws IOException {
        if (isEnableSsl() && getSslContext() == null) {
            SslContextBuilder builder = SslContextBuilder.forServer();
            if (!Util.isNullOrBlank(getSslPem())) {
                String[] params = getSslPem().split(";");
                String password = null;
                if (params.length == 3) {
                    password = params[2].trim();
                    if (password.length() == 0) {
                        password = null;
                    }
                } else if (params.length != 2) {
                    throw new IllegalArgumentException("sslPem config error");
                }
                InputStream privateKey = FileUtil.readInputStreamByCls(params[0], classLoader);
                InputStream certificate = FileUtil.readInputStreamByCls(params[1], classLoader);
                builder.keyManager(privateKey, certificate, password);
                builder.applicationProtocols(applicationProtocols);
                SslContext sslContext = builder.build();
                setSslContext(sslContext);
                return;
            }
            if (!Util.isNullOrBlank(getSslKeystore())) {
                String keystoreInfo = getSslKeystore();
                String[] params = keystoreInfo.split(";");
                if (params.length != 4) {
                    throw new IllegalArgumentException("sslKeystore config error");
                }
                File storeFile = FileUtil.readFileByCls(params[0], classLoader);
                FileInputStream is = new FileInputStream(storeFile);
                builder.keyManager(is, params[1], params[2], params[3]);
                builder.applicationProtocols(applicationProtocols);
                SslContext sslContext = builder.build();
                setSslContext(sslContext);
                return;
            }
            throw new IllegalArgumentException("ssl enabled,but there is no config for");
        }
    }

    abstract boolean isActive();

    public boolean isEnableHeartbeatLog() {
        return enableHeartbeatLog;
    }

    public boolean isEnableSsl() {
        return enableSsl;
    }

    public boolean isPrintConfig() {
        return printConfig;
    }

    public Object removeAttribute(Object key) {
        return this.attributes.remove(key);
    }

    public void setApplicationProtocols(String[] applicationProtocols) {
        checkNotRunning();
        this.applicationProtocols = applicationProtocols;
    }

    public void setAttribute(Object key, Object value) {
        this.attributes.put(key, value);
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

    public void setExecutorEventLoopGroup(EventLoopGroup executorEventLoopGroup) {
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
        this.processorGroup = nioEventLoopGroup;
    }

    public void setOpenSslPath(String openSslPath) {
        checkNotRunning();
        this.openSslPath = openSslPath;
    }

    public void setPort(int port) {
        checkNotRunning();
        this.port = port;
    }

    public void setPrintConfig(boolean printConfig) {
        checkNotRunning();
        this.printConfig = printConfig;
    }

    public void setProperties(Properties properties) {
        checkNotRunning();
        this.properties = properties;
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

    public void setSslPem(String sslPem) {
        checkNotRunning();
        this.sslPem = sslPem;
    }

    private String sslType() {
        return enableSsl ? SslContext.OPENSSL_AVAILABLE ? "openssl" : "jdkssl" : "false";
    }

    protected void stopEventLoopGroup(NioEventLoopGroup group) {
        if (group != null && !group.isSharable()) {
            Util.stop(group);
        }
    }

    public interface HeartBeatLogger {

        void logPing(Channel ch);

        void logPong(Channel ch);
    }

}
