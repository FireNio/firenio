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

import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.FileUtil;
import com.generallycloud.baseio.common.Properties;
import com.generallycloud.baseio.component.ChannelAcceptor;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.ConfigurationParser;
import com.generallycloud.baseio.component.NioEventLoopGroup;
import com.generallycloud.baseio.container.ApplicationIoEventHandle;
import com.generallycloud.baseio.container.BootstrapEngine;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public abstract class ApplicationBootstrapEngine implements BootstrapEngine {

    protected abstract void enrichSocketChannelContext(ChannelContext context);

    @Override
    public void bootstrap(String rootPath, String mode) throws Exception {
        Properties properties = FileUtil.readPropertiesByCls("server.properties");
        ChannelContext context = new ChannelContext();
        NioEventLoopGroup group = new NioEventLoopGroup();
        ConfigurationParser.parseConfiguration("server.", context, properties);
        ConfigurationParser.parseConfiguration("server.", group, properties);
        ChannelAcceptor acceptor = new ChannelAcceptor(context, group);
        ApplicationIoEventHandle handle = new ApplicationIoEventHandle(rootPath, mode);
        context.setIoEventHandle(handle);
        context.addLifeCycleListener(handle);
        try {
            enrichSocketChannelContext(context);
            if (context.getPort() == 0) {
                context.setPort(context.isEnableSsl() ? 443 : 80);
            }
            acceptor.bind();
        } catch (Throwable e) {
            Logger logger = LoggerFactory.getLogger(getClass());
            logger.error(e.getMessage(), e);
            CloseUtil.unbind(acceptor);
        }
    }

}
