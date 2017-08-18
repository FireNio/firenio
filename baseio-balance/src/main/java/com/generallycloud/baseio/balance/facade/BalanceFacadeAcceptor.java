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
package com.generallycloud.baseio.balance.facade;

import java.io.IOException;

import com.generallycloud.baseio.acceptor.SocketChannelAcceptor;
import com.generallycloud.baseio.balance.BalanceContext;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.LoggerUtil;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.log.LoggerFactory;

public class BalanceFacadeAcceptor {

    private boolean               running         = false;
    private BalanceContext        balanceContext  = null;
    private SocketChannelAcceptor channelAcceptor = null;

    public void start(BalanceContext balanceContext, SocketChannelContext facadeContext,
            SocketChannelContext reverseContext) throws IOException {

        if (balanceContext == null) {
            throw new IllegalArgumentException("null configuration");
        }

        synchronized (this) {

            if (running) {
                return;
            }

            this.balanceContext = balanceContext;

            this.balanceContext.getBalanceReverseAcceptor().start(reverseContext);

            this.channelAcceptor = new SocketChannelAcceptor(facadeContext);

            this.channelAcceptor.bind();

            LoggerUtil.prettyLog(LoggerFactory.getLogger(BalanceFacadeAcceptor.class),
                    "Balance Facade Acceptor startup completed ...");
        }

    }

    public void stop() {
        synchronized (this) {
            CloseUtil.unbind(channelAcceptor);
            this.balanceContext.getBalanceReverseAcceptor().stop();
        }
    }

    public BalanceContext getBalanceContext() {
        return balanceContext;
    }

    public SocketChannelAcceptor getAcceptor() {
        return channelAcceptor;
    }

}
