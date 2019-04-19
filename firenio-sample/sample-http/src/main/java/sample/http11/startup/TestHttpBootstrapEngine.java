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
import com.firenio.component.ChannelAcceptor;
import com.firenio.component.ChannelAliveListener;
import com.firenio.component.ConfigurationParser;
import com.firenio.component.LoggerChannelOpenListener;
import com.firenio.component.NioEventLoopGroup;
import com.firenio.concurrent.ThreadEventLoopGroup;
import com.firenio.container.BootstrapEngine;
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

    @Override
    public void bootstrap(final String rootPath, final String mode) throws Exception {
        DevelopConfig.NATIVE_DEBUG = true;
        DevelopConfig.BUF_DEBUG = true;
        Options.setEnableEpoll(true);
        Options.setEnableOpenssl(true);
        //        Options.setEnableUnsafeBuf(true);
        HttpDateUtil.start();
        final SpringHttpFrameHandle handle     = new SpringHttpFrameHandle();
        Properties                  properties = FileUtil.readPropertiesByCls("server.properties");
        NioEventLoopGroup           group      = new NioEventLoopGroup(true);
        ChannelAcceptor             context    = new ChannelAcceptor(group);
        ConfigurationParser.parseConfiguration("server.", context, properties);
        ConfigurationParser.parseConfiguration("server.", group, properties);
        context.setIoEventHandle(handle);
        context.addChannelEventListener(new WebSocketChannelListener());
        context.addChannelIdleEventListener(new ChannelAliveListener());
        context.addChannelEventListener(new LoggerChannelOpenListener());
        context.addChannelEventListener(new CountChannelListener());
        context.setExecutorEventLoopGroup(new ThreadEventLoopGroup());
        context.addLifeCycleListener(new LifeCycleListener() {
            @Override
            public void lifeCycleStarting(LifeCycle lifeCycle) {
                try {
                    handle.initialize(context, rootPath, mode);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }

            @Override
            public void lifeCycleStopped(LifeCycle lifeCycle) {
                handle.destroy(context);
            }
        });
        if (properties.getBooleanProperty("app.enableHttp2")) {
            context.addProtocolCodec(new Http2Codec());
            context.setApplicationProtocols(new String[]{"h2", "http/1.1"});
        } else {
            context.addProtocolCodec(new HttpCodec(4));
            context.addProtocolCodec(new WebSocketCodec());
        }
        context.bind();
        NetDataTransferServer.get().startup(group, 18088);
    }

}
