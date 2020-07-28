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
package sample.http11.startup;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import javax.net.ssl.SSLException;

import com.firenio.DevelopConfig;
import com.firenio.LifeCycle;
import com.firenio.LifeCycleListener;
import com.firenio.Options;
import com.firenio.codec.http11.HttpCodec;
import com.firenio.codec.http11.HttpDateUtil;
import com.firenio.codec.http11.WebSocketChannelListener;
import com.firenio.codec.http11.WebSocketCodec;
import com.firenio.codec.http2.Http2Codec;
import com.firenio.common.FileUtil;
import com.firenio.common.Properties;
import com.firenio.common.Util;
import com.firenio.component.ChannelAcceptor;
import com.firenio.component.ChannelAliveListener;
import com.firenio.component.ConfigurationParser;
import com.firenio.component.LoggerChannelOpenListener;
import com.firenio.component.NioEventLoopGroup;
import com.firenio.component.SslContext;
import com.firenio.component.SslContextBuilder;
import com.firenio.concurrent.ThreadEventLoopGroup;
import com.firenio.boot.BootstrapEngine;
import com.firenio.log.Logger;
import com.firenio.log.LoggerFactory;
import sample.http11.SpringHttpFrameHandle;
import sample.http11.proxy4cloud.NetDataTransferServer;
import sample.http11.service.CountChannelListener;

/**
 * @author wangkai
 */
public class TestHttpBootstrapEngine implements BootstrapEngine {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private ChannelAcceptor   context;
    private NioEventLoopGroup group;

    @Override
    public void bootstrap(String rootPath, boolean prodMode) throws Exception {
        ClassLoader cl    = this.getClass().getClassLoader();
        boolean     debug = Util.isTrueValue(System.getProperty("http.debug"));
        if (debug) {
            for (; debug; ) {
                Util.sleep(100);
            }
        }
        DevelopConfig.NATIVE_DEBUG = true;
        DevelopConfig.BUF_DEBUG = true;
        DevelopConfig.BUF_PATH_DEBUG = true;
        DevelopConfig.SSL_DEBUG = true;
        DevelopConfig.CHANNEL_DEBUG = true;
        DevelopConfig.DEBUG_ERROR = true;
        Options.setEnableEpoll(true);
        Options.setEnableUnsafe(true);
        Options.setEnableOpenssl(true);
        Options.setBufThreadYield(true);
        //        Options.setEnableUnsafeBuf(true);
        HttpDateUtil.start();
        final SpringHttpFrameHandle handle     = new SpringHttpFrameHandle();
        Properties                  properties = FileUtil.readPropertiesByCls("server.properties", cl);
        NioEventLoopGroup           group      = new NioEventLoopGroup(true);
        ChannelAcceptor             context    = new ChannelAcceptor(group);
        ConfigurationParser.parseConfiguration("server.", context, properties);
        ConfigurationParser.parseConfiguration("server.", group, properties);
        context.setIoEventHandle(handle);
        context.addChannelIdleEventListener(new ChannelAliveListener());
        context.addChannelEventListener(new WebSocketChannelListener());
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.addChannelEventListener(new CountChannelListener());
        context.setExecutorGroup(new ThreadEventLoopGroup());
        context.addLifeCycleListener(new LifeCycleListener() {
            @Override
            public void lifeCycleStarting(LifeCycle lifeCycle) {
                try {
                    handle.initialize(context, rootPath, prodMode);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }

            @Override
            public void lifeCycleStopped(LifeCycle lifeCycle) {
                handle.destroy(context);
            }
        });
        String[] applicationProtocols = null;
        if (properties.getBooleanProperty("app.enableHttp2")) {
            context.addProtocolCodec(new Http2Codec());
            applicationProtocols = new String[]{"h2", "http/1.1"};
        } else {
            context.addProtocolCodec(new HttpCodec(4));
            context.addProtocolCodec(new WebSocketCodec());
        }
        int    defaultPort = 80;
        String pem         = properties.getProperty("server.sslPem");
        if (!Util.isNullOrBlank(pem)) {
            defaultPort = 443;
            SslContext sslContext = loadSslContextFromPem(pem, applicationProtocols, cl);
            context.setSslContext(sslContext);
        }
        int port = properties.getIntegerProperty("server.port", defaultPort);
        context.setPort(port);
        try {
            context.bind();
        } catch (Exception e) {
            HttpDateUtil.stop();
            group.stop();
            throw e;
        }
        this.group = group;
        this.context = context;
        if (properties.getBooleanProperty("app.proxy")) {
            NetDataTransferServer.get().startup(group, 18088);
        }
    }

    public synchronized void shutdown() {
        HttpDateUtil.stop();
        Util.unbind(context);
        Util.stop(group);
    }

    //server.sslPem=localhost.key;localhost.crt;
    private SslContext loadSslContextFromPem(String pem, String[] applicationProtocols, ClassLoader classLoader) throws SSLException {
        SslContextBuilder builder  = SslContextBuilder.forServer();
        String[]          params   = pem.split(";");
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
        return builder.build();
    }

    //#server.sslKeystore=file;store_password;alias;key_password
    private SslContext loadSslContextFromKeyStore(String keyStore, String[] applicationProtocols, ClassLoader classLoader) throws Exception {
        SslContextBuilder builder = SslContextBuilder.forServer();
        String[]          params  = keyStore.split(";");
        if (params.length != 4) {
            throw new IllegalArgumentException("sslKeystore config error");
        }
        File            storeFile = FileUtil.readFileByCls(params[0], classLoader);
        FileInputStream is        = new FileInputStream(storeFile);
        builder.keyManager(is, params[1], params[2], params[3]);
        builder.applicationProtocols(applicationProtocols);
        return builder.build();
    }

}
