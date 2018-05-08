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
package com.generallycloud.sample.baseio.http11;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.generallycloud.baseio.component.FutureAcceptor;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.container.http11.HttpFutureAcceptor;
import com.generallycloud.baseio.protocol.Future;
import com.generallycloud.baseio.protocol.NamedFuture;

public class SpringHttpFutureAcceptor extends HttpFutureAcceptor {

    private ClassPathXmlApplicationContext applicationContext;
    private boolean                        checkFilter = true;
    private FutureAcceptor                 filter;

    @Override
    public void accept(SocketSession session, Future future) throws Exception {
        NamedFuture f = (NamedFuture) future;
        if (checkFilter) {
            checkFilter = false;
            filter = getFutureAcceptor("http-filter");
        }
        if (filter != null) {
            filter.accept(session, future);
            if (future.flushed()) {
                return;
            }
        }
        FutureAcceptor acceptor = getFutureAcceptor(f.getFutureName());
        if (acceptor == null) {
            acceptHtml(session, f);
            return;
        }
        acceptor.accept(session, future);
    }

    private FutureAcceptor getFutureAcceptor(String name) {
        return (FutureAcceptor) ContextUtil.getBean(name);
    }

    @Override
    protected void destroy(SocketChannelContext context, boolean redeploy) {
        applicationContext.destroy();
        super.destroy(context, redeploy);
    }

    @Override
    protected void initialize(SocketChannelContext context, boolean redeploy) throws Exception {
        super.initialize(context, redeploy);
        System.setProperty("org.apache.commons.logging.log", Sl4jLogger.class.getName());
        Thread.currentThread().setContextClassLoader(null); //for spring
        applicationContext = new ClassPathXmlApplicationContext("classpath:spring-core.xml");
        applicationContext.start();
    }

}
