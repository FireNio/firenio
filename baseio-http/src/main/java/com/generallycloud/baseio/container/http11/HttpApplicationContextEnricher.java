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

import java.util.Set;

import com.generallycloud.baseio.codec.http11.ServerHTTPProtocolFactory;
import com.generallycloud.baseio.codec.http11.future.WebSocketBeatFutureFactory;
import com.generallycloud.baseio.component.LoggerSocketSEListener;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSessionAliveSEListener;
import com.generallycloud.baseio.container.ApplicationContext;
import com.generallycloud.baseio.container.ApplicationContextEnricher;
import com.generallycloud.baseio.container.BlackIPFilter;
import com.generallycloud.baseio.container.http11.service.FutureAcceptorHttpFilter;

/**
 * @author wangkai
 *
 */
public class HttpApplicationContextEnricher implements ApplicationContextEnricher {

    @Override
    public void enrich(ApplicationContext context) {

        SocketChannelContext channelContext = context.getChannelContext();

        context.setServiceFilter(new FutureAcceptorHttpFilter());
        
        context.setExceptionCaughtHandle(new HttpExceptionCaughtHandle());

        //FIXME 重复的
        Set<String> blackIPs = context.getBlackIPs();

        if (blackIPs != null && !blackIPs.isEmpty()) {
            channelContext.addSessionEventListener(new BlackIPFilter(blackIPs));
        }

        channelContext.setBeatFutureFactory(new WebSocketBeatFutureFactory());

        channelContext.addSessionEventListener(new LoggerSocketSEListener());

        channelContext.addSessionIdleEventListener(new SocketSessionAliveSEListener());

        channelContext.setProtocolFactory(new ServerHTTPProtocolFactory());

    }

}
