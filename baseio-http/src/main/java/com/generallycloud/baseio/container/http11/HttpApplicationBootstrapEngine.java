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
package com.generallycloud.baseio.container.http11;

import com.generallycloud.baseio.codec.http11.ServerHttpCodec;
import com.generallycloud.baseio.codec.http11.WebSocketChannelEListener;
import com.generallycloud.baseio.component.ChannelAliveIdleEventListener;
import com.generallycloud.baseio.component.ChannelContext;
import com.generallycloud.baseio.component.LoggerSocketSEListener;
import com.generallycloud.baseio.concurrent.ExecutorPoolEventLoopGroup;
import com.generallycloud.baseio.container.ApplicationIoEventHandle;
import com.generallycloud.baseio.container.bootstrap.ApplicationBootstrapEngine;
import com.generallycloud.baseio.container.configuration.FileSystemACLoader;

/**
 * @author wangkai
 *
 */
public class HttpApplicationBootstrapEngine extends ApplicationBootstrapEngine {

    @Override
    protected void enrichSocketChannelContext(ChannelContext context) {
        ApplicationIoEventHandle handle = (ApplicationIoEventHandle) context.getIoEventHandle();
        handle.setApplicationExtLoader(new HttpExtLoader());
        handle.setApplicationConfigurationLoader(new FileSystemACLoader());
        handle.setAppOnRedeployService(new HttpOnRedeployAcceptor());
        context.addChannelEventListener(new LoggerSocketSEListener());
        context.getNioEventLoopGroup().setIdleTime(1000 * 60 * 30);
        context.addChannelIdleEventListener(new ChannelAliveIdleEventListener());
        context.addChannelEventListener(new WebSocketChannelEListener());
        context.setProtocolCodec(new ServerHttpCodec());
        context.setExecutorEventLoopGroup(new ExecutorPoolEventLoopGroup("http-event-processor", 16,
                64, 1024 * 64, 1000 * 60 * 30));
    }

}
