/*
 * Copyright 2015 The FireNio Project
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
package com.firenio.component;

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

import com.firenio.LifeCycle;
import com.firenio.Options;
import com.firenio.common.Assert;
import com.firenio.common.FileUtil;
import com.firenio.common.Properties;
import com.firenio.common.Util;
import com.firenio.concurrent.EventLoop;
import com.firenio.concurrent.EventLoopGroup;
import com.firenio.log.Logger;
import com.firenio.log.LoggerFactory;

public abstract class ChannelContext extends LifeCycle implements Configuration {

    private String[]                   applicationProtocols;
    private Map<Object, Object>        attributes         = new HashMap<>();
    private List<ChannelEventListener> cels               = new ArrayList<>();
    private ChannelManager             channelManager     = new ChannelManager();
    private Charset                    charset            = Util.UTF8;
    private List<ChannelIdleListener>  ciels              = new ArrayList<>();
    private Map<String, ProtocolCodec> codecs             = new HashMap<>();
    private ProtocolCodec              defaultCodec;
    private boolean                    enableHeartbeatLog = true;
    private boolean                    enableSsl;
    //是否启用work event loop，如果启用，则frame在work event loop中处理
    private EventLoopGroup             executorGroup;
    private HeartBeatLogger            heartBeatLogger;
    private String                     host;
    private boolean                    initialized;
    private IoEventHandle              ioEventHandle      = DefaultIoEventHandle.get();
    private Logger                     logger             = LoggerFactory.getLogger(getClass());
    private int                        maxWriteBacklog    = Integer.MAX_VALUE;
    private String                     openSslPath;
    private int                        port;
    private boolean                    printConfig        = true;
    private NioEventLoopGroup          processorGroup;
    private Properties                 properties;
    private InetSocketAddress          serverAddress;
    private SslContext                 sslContext;
    private String                     sslKeystore;
    private String                     sslPem;
    private long                       startupTime        = System.currentTimeMillis();

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
        NioEventLoopGroup g             = this.processorGroup;
        int               eventLoopSize = g.getEventLoopSize();
        if (Util.isNullOrBlank(host)) {
            this.serverAddress = new InetSocketAddress(port);
        } else {
            this.serverAddress = new InetSocketAddress(host, port);
        }
        Util.start(executorGroup);
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
            logger.info("enable epoll          : [ {} ]", Native.EPOLL_AVAILABLE);
            logger.info("channel idle          : [ {} ]", g.getIdleTime());
            logger.info("host and port         : [ {}:{} ]", getHost(), port);
            if (g.isEnableMemoryPool()) {
                long   memoryPoolCapacity = g.getMemoryPoolCapacity() * g.getEventLoopSize();
                long   memoryPoolByteSize = memoryPoolCapacity * g.getMemoryPoolUnit();
                double memoryPoolSize     = memoryPoolByteSize / (1024 * 1024);
                logger.info("memory pool           : [ {}/{}/{}M ({}) ]", g.getMemoryPoolUnit(), memoryPoolCapacity, BigDecimal.valueOf(memoryPoolSize).setScale(2, BigDecimal.ROUND_HALF_UP), getByteBufPoolType(g));
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

    private String getByteBufPoolType(NioEventLoopGroup g) {
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
        Util.stop(executorGroup);
        this.attributes.clear();
    }

    public String[] getApplicationProtocols() {
        return applicationProtocols;
    }

    public void setApplicationProtocols(String[] applicationProtocols) {
        checkNotRunning();
        this.applicationProtocols = applicationProtocols;
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

    public void setCharset(Charset charset) {
        checkNotRunning();
        this.charset = charset;
    }

    public ProtocolCodec getDefaultCodec() {
        return defaultCodec;
    }

    public EventLoopGroup getExecutorGroup() {
        return executorGroup;
    }

    public void setExecutorGroup(EventLoopGroup executorGroup) {
        checkNotRunning();
        this.executorGroup = executorGroup;
    }

    public HeartBeatLogger getHeartBeatLogger() {
        return heartBeatLogger;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        checkNotRunning();
        this.host = host;
    }

    public IoEventHandle getIoEventHandle() {
        return ioEventHandle;
    }

    public void setIoEventHandle(IoEventHandle ioEventHandle) {
        checkNotRunning();
        this.ioEventHandle = ioEventHandle;
    }

    public int getMaxWriteBacklog() {
        return maxWriteBacklog;
    }

    public void setMaxWriteBacklog(int maxWriteBacklog) {
        checkNotRunning();
        this.maxWriteBacklog = maxWriteBacklog;
    }

    public EventLoop getNextExecutorEventLoop() {
        if (executorGroup == null) {
            return null;
        }
        return executorGroup.getNext();
    }

    public String getOpenSslPath() {
        return openSslPath;
    }

    public void setOpenSslPath(String openSslPath) {
        checkNotRunning();
        this.openSslPath = openSslPath;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        checkNotRunning();
        this.port = port;
    }

    public NioEventLoopGroup getProcessorGroup() {
        return processorGroup;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        checkNotRunning();
        this.properties = properties;
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

    public void setSslContext(SslContext sslContext) {
        checkNotRunning();
        if (sslContext == null) {
            throw new IllegalArgumentException("null sslContext");
        }
        this.sslContext = sslContext;
        this.enableSsl = true;
    }

    public String getSslKeystore() {
        return sslKeystore;
    }

    public void setSslKeystore(String sslKeystore) {
        checkNotRunning();
        this.sslKeystore = sslKeystore;
    }

    public String getSslPem() {
        return sslPem;
    }

    public void setSslPem(String sslPem) {
        checkNotRunning();
        this.sslPem = sslPem;
    }

    public long getStartupTime() {
        return startupTime;
    }

    private void initHeartBeatLogger() {
        if (isEnableHeartbeatLog()) {
            heartBeatLogger = new HeartBeatLogger() {
                final Logger logger = LoggerFactory.getLogger("hb");

                @Override
                public void logPingFrom(Channel ch) {
                    logger.info("hb req from: {}", ch);
                }

                @Override
                public void logPingTo(Channel ch) {
                    logger.info("hb send req: {}", ch);
                }

                @Override
                public void logPongFrom(Channel ch) {
                    logger.info("hb res from: {}", ch);
                }

                @Override
                public void logPongTo(Channel ch) {
                    logger.info("hb send res: {}", ch);
                }

            };
        } else {
            heartBeatLogger = new HeartBeatLogger() {
                final Logger logger = LoggerFactory.getLogger("hb");

                @Override
                public void logPingFrom(Channel ch) {
                    logger.debug("hb req from: {}", ch);
                }

                @Override
                public void logPingTo(Channel ch) {
                    logger.debug("hb send req: {}", ch);
                }

                @Override
                public void logPongFrom(Channel ch) {
                    logger.debug("hb res from: {}", ch);
                }

                @Override
                public void logPongTo(Channel ch) {
                    logger.debug("hb send res: {}", ch);
                }

            };
        }
    }

    private void initSslContext(ClassLoader classLoader) throws IOException {
        if (getSslContext() == null) {
            if (!Util.isNullOrBlank(getSslPem())) {
                SslContextBuilder builder  = SslContextBuilder.forServer();
                String[]          params   = getSslPem().split(";");
                String            password = null;
                if (params.length == 3) {
                    password = params[2].trim();
                    if (password.length() == 0) {
                        password = null;
                    }
                } else if (params.length != 2) {
                    throw new IllegalArgumentException("sslPem config error");
                }
                InputStream privateKey  = FileUtil.readInputStreamByCls(params[0], classLoader);
                InputStream certificate = FileUtil.readInputStreamByCls(params[1], classLoader);
                builder.keyManager(privateKey, certificate, password);
                builder.applicationProtocols(applicationProtocols);
                SslContext sslContext = builder.build();
                setSslContext(sslContext);
            } else if (!Util.isNullOrBlank(getSslKeystore())) {
                SslContextBuilder builder      = SslContextBuilder.forServer();
                String            keystoreInfo = getSslKeystore();
                String[]          params       = keystoreInfo.split(";");
                if (params.length != 4) {
                    throw new IllegalArgumentException("sslKeystore config error");
                }
                File            storeFile = FileUtil.readFileByCls(params[0], classLoader);
                FileInputStream is        = new FileInputStream(storeFile);
                builder.keyManager(is, params[1], params[2], params[3]);
                builder.applicationProtocols(applicationProtocols);
                SslContext sslContext = builder.build();
                setSslContext(sslContext);
            }
        }
        if (getPort() == 0) {
            setPort(isEnableSsl() ? 443 : 80);
        }
    }

    abstract boolean isActive();

    public boolean isEnableHeartbeatLog() {
        return enableHeartbeatLog;
    }

    public void setEnableHeartbeatLog(boolean enableHeartbeatLog) {
        checkNotRunning();
        this.enableHeartbeatLog = enableHeartbeatLog;
    }

    public boolean isEnableSsl() {
        return enableSsl;
    }

    public boolean isPrintConfig() {
        return printConfig;
    }

    public void setPrintConfig(boolean printConfig) {
        checkNotRunning();
        this.printConfig = printConfig;
    }

    public Object removeAttribute(Object key) {
        return this.attributes.remove(key);
    }

    public void setAttribute(Object key, Object value) {
        this.attributes.put(key, value);
    }

    public void setNioEventLoopGroup(NioEventLoopGroup nioEventLoopGroup) {
        checkNotRunning();
        this.processorGroup = nioEventLoopGroup;
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

        void logPingFrom(Channel ch);

        void logPingTo(Channel ch);

        void logPongFrom(Channel ch);

        void logPongTo(Channel ch);
    }

}
