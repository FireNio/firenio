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
package com.generallycloud.baseio.container.bootstrap;

import java.io.File;

import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.FileUtil;
import com.generallycloud.baseio.common.Properties;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.ChannelAcceptor;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.NioEventLoopGroup;
import com.generallycloud.baseio.component.ssl.SSLUtil;
import com.generallycloud.baseio.component.ssl.SslContext;
import com.generallycloud.baseio.configuration.Configuration;
import com.generallycloud.baseio.configuration.ConfigurationParser;
import com.generallycloud.baseio.container.ApplicationIoEventHandle;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public abstract class ApplicationBootstrapEngine implements BootstrapEngine {

    protected abstract void enrichSocketChannelContext(ChannelContext context);

    @Override
    public void bootstrap(String rootPath, String mode) throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        Properties properties = FileUtil.readPropertiesByCls("server.properties");
        Configuration cfg = new Configuration();
        ConfigurationParser.parseConfiguration("server.", cfg, properties);
        ChannelContext context = new ChannelContext(cfg);
        ChannelAcceptor acceptor = new ChannelAcceptor(context, new NioEventLoopGroup());
        context.setIoEventHandle(new ApplicationIoEventHandle(rootPath, mode));
        try {
            if (cfg.isEnableSsl()) {
                context.getNioEventLoopGroup().setEnableSsl(true);
                if (!StringUtil.isNullOrBlank(cfg.getCertCrt())) {
                    File certificate = FileUtil.readFileByCls(cfg.getCertCrt(), classLoader);
                    File privateKey = FileUtil.readFileByCls(cfg.getCertKey(), classLoader);
                    SslContext sslContext = SSLUtil.initServer(privateKey, certificate);
                    context.setSslContext(sslContext);
                } else {
                    String keystoreInfo = cfg.getSslKeystore();
                    if (StringUtil.isNullOrBlank(keystoreInfo)) {
                        throw new IllegalArgumentException("ssl enabled,but no config for");
                    }
                    String[] params = keystoreInfo.split(";");
                    if (params.length != 4) {
                        throw new IllegalArgumentException("SERVER_SSL_KEYSTORE config error");
                    }
                    File storeFile = FileUtil.readFileByCls(params[0], classLoader);
                    SslContext sslContext = SSLUtil.initServer(storeFile, params[1], params[2],
                            params[3]);
                    context.setSslContext(sslContext);
                }
            }
            enrichSocketChannelContext(context);
            int port = cfg.getPort();
            if (port == 0) {
                port = cfg.isEnableSsl() ? 443 : 80;
            }
            cfg.setPort(port);
            acceptor.bind();
        } catch (Throwable e) {
            Logger logger = LoggerFactory.getLogger(getClass());
            logger.error(e.getMessage(), e);
            CloseUtil.unbind(acceptor);
        }
    }

}
