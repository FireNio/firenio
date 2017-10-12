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
package com.generallycloud.baseio.container.startup;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import com.generallycloud.baseio.acceptor.SocketChannelAcceptor;
import com.generallycloud.baseio.common.ClassUtil;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.FileUtil;
import com.generallycloud.baseio.common.Properties;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.BootstrapEngine;
import com.generallycloud.baseio.component.NioSocketChannelContext;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.ssl.SSLUtil;
import com.generallycloud.baseio.component.ssl.SslContext;
import com.generallycloud.baseio.configuration.PropertiesSCLoader;
import com.generallycloud.baseio.configuration.ServerConfiguration;
import com.generallycloud.baseio.container.ApplicationContext;
import com.generallycloud.baseio.container.ApplicationContextEnricher;
import com.generallycloud.baseio.container.ApplicationExtLoader;
import com.generallycloud.baseio.container.ApplicationIoEventHandle;
import com.generallycloud.baseio.container.DefaultExtLoader;
import com.generallycloud.baseio.container.configuration.ApplicationConfigurationLoader;
import com.generallycloud.baseio.container.configuration.FileSystemACLoader;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public class ApplicationBootstrapEngine implements BootstrapEngine {

    @Override
    public void bootstrap(String rootPath, boolean deployModel) throws Exception {
        
        ClassLoader classLoader = getClass().getClassLoader();

        Properties serverProperties = FileUtil.readPropertiesByCls("server.properties");

        ServerConfiguration sc = new ServerConfiguration();
                
        new PropertiesSCLoader("SERVER").loadConfiguration(sc,serverProperties);

        ApplicationContext applicationContext = new ApplicationContext(rootPath);

        applicationContext.setDeployModel(deployModel);

        SocketChannelContext channelContext = new NioSocketChannelContext(sc);
        //		SocketChannelContext channelContext = new AioSocketChannelContext(sc);

        SocketChannelAcceptor acceptor = new SocketChannelAcceptor(channelContext);

        try {

            Properties intfProperties = FileUtil.readPropertiesByCls("intf.properties");

            applicationContext.setBlackIPs(loadBlackIPs());

            applicationContext.setChannelContext(channelContext);

            ApplicationConfigurationLoader acLoader = loadConfigurationLoader(
                    intfProperties.getProperty("intf.ApplicationConfigurationLoader"));

            ApplicationExtLoader applicationExtLoader = loadApplicationExtLoader(
                    intfProperties.getProperty("intf.ApplicationExtLoader"));

            ApplicationContextEnricher enricher = loadApplicationContextEnricher(
                    intfProperties.getProperty("intf.ApplicationContextEnricher"));

            applicationContext.setApplicationExtLoader(applicationExtLoader);

            applicationContext.setApplicationConfigurationLoader(acLoader);

            enricher.enrich(applicationContext);

            channelContext
                    .setIoEventHandleAdaptor(new ApplicationIoEventHandle(applicationContext));

            if (sc.isSERVER_ENABLE_SSL()) {

                File certificate = FileUtil.readFileByCls(sc.getSERVER_CERT_CRT(),classLoader);
                File privateKey = FileUtil.readFileByCls(sc.getSERVER_CERT_KEY(),classLoader);

                SslContext sslContext = SSLUtil.initServer(privateKey, certificate);

                channelContext.setSslContext(sslContext);
            }

            sc.setSERVER_PORT(getServerPort(sc.getSERVER_PORT(), sc.isSERVER_ENABLE_SSL()));

            acceptor.bind();

        } catch (Throwable e) {

            Logger logger = LoggerFactory.getLogger(getClass());

            logger.error(e.getMessage(), e);

            CloseUtil.unbind(acceptor);
        }
    }

    private int getServerPort(int port, boolean enableSSL) {
        if (port != 0) {
            return port;
        }
        return enableSSL ? 443 : 80;
    }

    private ApplicationContextEnricher loadApplicationContextEnricher(String className)
            throws Exception {
        Class<?> clazz = ClassUtil.forName(className);
        if (clazz == null) {
            throw new Exception("intf.ApplicationContextEnricher is empty");
        }
        return (ApplicationContextEnricher) ClassUtil.newInstance(clazz);
    }

    private ApplicationExtLoader loadApplicationExtLoader(String className) throws Exception {
        Class<?> clazz = ClassUtil.forName(className, DefaultExtLoader.class);
        return (ApplicationExtLoader) ClassUtil.newInstance(clazz);
    }

    private Set<String> loadBlackIPs() {
        try {
            String content = FileUtil.readStringByCls("black-ip.cfg");
            if (StringUtil.isNullOrBlank(content)) {
                return null;
            }
            String[] lines = content.split("\n");
            Set<String> result = new HashSet<>();
            for (String l : lines) {
                if (StringUtil.isNullOrBlank(l)) {
                    continue;
                }
                result.add(l.trim().replace("\r", "").replace("\t", "\t"));
            }
            return result;
        } catch (Exception e) {}
        return null;
    }

    private ApplicationConfigurationLoader loadConfigurationLoader(String className) {
        Class<?> clazz = ClassUtil.forName(className, FileSystemACLoader.class);
        return (ApplicationConfigurationLoader) ClassUtil.newInstance(clazz);
    }

}
