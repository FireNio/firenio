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
package com.generallycloud.baseio.container.http11.service;

import com.generallycloud.baseio.codec.http11.future.HttpFuture;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.container.http11.HttpContext;
import com.generallycloud.baseio.container.http11.HttpSession;
import com.generallycloud.baseio.container.http11.HttpSessionManager;
import com.generallycloud.baseio.container.service.FutureAcceptorService;
import com.generallycloud.baseio.protocol.Future;

public abstract class HttpFutureAcceptorService extends FutureAcceptorService {

    private HttpContext context = HttpContext.getInstance();

    @Override
    public void accept(SocketSession session, Future future) throws Exception {

        HttpSessionManager manager = context.getHttpSessionManager();

        HttpFuture httpReadFuture = (HttpFuture) future;

        HttpSession httpSession = manager.getHttpSession(context, session, httpReadFuture);

        doAccept(httpSession, httpReadFuture);
    }

    protected abstract void doAccept(HttpSession session, HttpFuture future) throws Exception;

}
