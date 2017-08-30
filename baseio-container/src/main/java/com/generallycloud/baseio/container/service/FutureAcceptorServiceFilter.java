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
package com.generallycloud.baseio.container.service;

import java.io.IOException;

import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.container.ApplicationContext;
import com.generallycloud.baseio.container.InitializeUtil;
import com.generallycloud.baseio.container.RESMessage;
import com.generallycloud.baseio.container.configuration.Configuration;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.Future;
import com.generallycloud.baseio.protocol.NamedFuture;

public class FutureAcceptorServiceFilter extends FutureAcceptorFilter {

    private Logger                      logger                = LoggerFactory.getLogger(getClass());
    private FutureAcceptorServiceLoader acceptorServiceLoader = new FutureAcceptorServiceLoader();

    public FutureAcceptorServiceFilter() {
        this.setSortIndex(Integer.MAX_VALUE);
    }

    @Override
    protected void accept(SocketSession session, NamedFuture future) throws Exception {
        String serviceName = future.getFutureName();
        if (StringUtil.isNullOrBlank(serviceName)) {
            accept404(session, future, serviceName);
        } else {
            accept(serviceName, session, future);
        }
    }

    private void accept(String serviceName, SocketSession session, NamedFuture future)
            throws Exception {
        FutureAcceptorService acceptor = acceptorServiceLoader.getFutureAcceptor(serviceName);
        if (acceptor == null) {
            accept404(session, future, serviceName);
        } else {
            acceptor.accept(session, future);
        }
    }

    protected void accept404(SocketSession session, NamedFuture future, String serviceName)
            throws IOException {
        logger.info("service name [{}] not found", serviceName);
        RESMessage message = new RESMessage(404, "service name not found :" + serviceName);
        flush(session, future, message);
    }

    private void flush(SocketSession session, Future future, RESMessage message)
            throws IOException {
        future.write(message.toString());
        session.flush(future);
    }

    @Override
    public void destroy(ApplicationContext context, Configuration config) throws Exception {
        InitializeUtil.destroy(acceptorServiceLoader, context);
    }

    @Override
    public void initialize(ApplicationContext context, Configuration config) throws Exception {
        acceptorServiceLoader.initialize(context, config);
    }

    public FutureAcceptorServiceLoader getFutureAcceptorServiceLoader() {
        return acceptorServiceLoader;
    }

}
