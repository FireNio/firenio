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
package com.generallycloud.baseio.container.implementation;

import com.generallycloud.baseio.acceptor.ChannelAcceptor;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.LoggerUtil;
import com.generallycloud.baseio.common.ThreadUtil;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.container.service.FutureAcceptorService;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;
import com.generallycloud.baseio.protocol.Future;

public class SystemStopServerServlet extends FutureAcceptorService {

    private Logger logger = LoggerFactory.getLogger(SystemStopServerServlet.class);

    public SystemStopServerServlet() {
        this.setServiceName("/system-stop-server.auth");
    }

    @Override
    public void accept(SocketSession session, Future future) throws Exception {

        SocketChannelContext context = session.getContext();

        future.write("server is stopping");

        session.flush(future);

        new Thread(new StopServer(context)).start();
    }

    private class StopServer implements Runnable {

        private SocketChannelContext context = null;

        public StopServer(SocketChannelContext context) {
            this.context = context;
        }

        @Override
        public void run() {

            ThreadUtil.sleep(500);

            LoggerUtil.prettyLog(logger, "execute stop service");

            String[] words = new String[] { "5", "4", "3", "2", "1" };

            for (int i = 0; i < 5; i++) {

                LoggerUtil.prettyLog(logger, "service will stop after {} seconds", words[i]);

                ThreadUtil.sleep(1000);
            }

            CloseUtil.unbind((ChannelAcceptor) context.getChannelService());

        }
    }
}
