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
package com.generallycloud.sample.baseio.http11.startup;

import com.generallycloud.baseio.AbstractLifeCycleListener;
import com.generallycloud.baseio.LifeCycle;
import com.generallycloud.baseio.codec.http11.ServerHttpCodec;
import com.generallycloud.baseio.codec.http11.WebSocketChannelListener;
import com.generallycloud.baseio.codec.http2.Http2Codec;
import com.generallycloud.baseio.common.FileUtil;
import com.generallycloud.baseio.common.Properties;
import com.generallycloud.baseio.component.ChannelAcceptor;
import com.generallycloud.baseio.component.ChannelAliveIdleEventListener;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.ConfigurationParser;
import com.generallycloud.baseio.component.LoggerChannelOpenListener;
import com.generallycloud.baseio.component.NioEventLoopGroup;
import com.generallycloud.baseio.container.BootstrapEngine;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.sample.baseio.http11.SpringHttpFrameHandle;

/**
 * @author wangkai
 *
 */
public class TestHttpBootstrapEngine implements BootstrapEngine {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void bootstrap(final String rootPath, final String mode) throws Exception {
        final ChannelContext context = new ChannelContext();
        final SpringHttpFrameHandle handle = new SpringHttpFrameHandle();
        Properties properties = FileUtil.readPropertiesByCls("server.properties");
        NioEventLoopGroup group = new NioEventLoopGroup();
        ConfigurationParser.parseConfiguration("server.", context, properties);
        ConfigurationParser.parseConfiguration("server.", group, properties);
        ChannelAcceptor acceptor = new ChannelAcceptor(context, group);
        context.setProtocolCodec(new ServerHttpCodec(4));
        context.setIoEventHandle(handle);
        context.addChannelEventListener(new WebSocketChannelListener());
        context.addChannelIdleEventListener(new ChannelAliveIdleEventListener());
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.addLifeCycleListener(new AbstractLifeCycleListener() {
            @Override
            public void lifeCycleStopped(LifeCycle lifeCycle) {
                handle.destroy(context);
            }

            @Override
            public void lifeCycleStarted(LifeCycle lifeCycle) {
                try {
                    handle.initialize(rootPath, mode);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        });
        if (properties.getBooleanProperty("app.enableHttp2")) {
            context.setProtocolCodec(new Http2Codec());
            context.setApplicationProtocols(new String[] { "h2", "http/1.1" });
        } else {
            context.setProtocolCodec(new ServerHttpCodec());
        }
        if (context.getPort() == 0) {
            context.setPort(context.isEnableSsl() ? 443 : 80);
        }
        acceptor.bind();
    }

}
