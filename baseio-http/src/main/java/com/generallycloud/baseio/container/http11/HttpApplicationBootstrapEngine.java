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

import com.generallycloud.baseio.codec.http11.ServerHTTPProtocolFactory;
import com.generallycloud.baseio.codec.http11.future.WebSocketBeatFutureFactory;
import com.generallycloud.baseio.component.LoggerSocketSEListener;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSessionAliveSEListener;
import com.generallycloud.baseio.container.ApplicationIoEventHandle;
import com.generallycloud.baseio.container.configuration.FileSystemACLoader;
import com.generallycloud.baseio.container.startup.ApplicationBootstrapEngine;

/**
 * @author wangkai
 *
 */
public class HttpApplicationBootstrapEngine extends ApplicationBootstrapEngine {

    @Override
    protected void enrichSocketChannelContext(SocketChannelContext context) {
        ApplicationIoEventHandle handle = 
                (ApplicationIoEventHandle) context.getIoEventHandleAdaptor();
        handle.setApplicationExtLoader(new HttpExtLoader());
        handle.setApplicationConfigurationLoader(new FileSystemACLoader());
        handle.setAppOnRedeployService(new HttpOnRedeployAcceptor());
        context.setBeatFutureFactory(new WebSocketBeatFutureFactory());
        context.addSessionEventListener(new LoggerSocketSEListener());
        context.addSessionIdleEventListener(new SocketSessionAliveSEListener());
        context.setProtocolFactory(new ServerHTTPProtocolFactory());
    }

}
