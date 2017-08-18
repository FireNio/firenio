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

import com.generallycloud.baseio.balance.BalanceContext;
import com.generallycloud.baseio.balance.ChannelLostFutureFactory;
import com.generallycloud.baseio.balance.reverse.BalanceReverseSocketSession;
import com.generallycloud.baseio.balance.router.BalanceRouter;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.component.SocketSessionEventListenerAdapter;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public class BalanceFacadeAcceptorSEListener extends SocketSessionEventListenerAdapter {

    private Logger         logger = LoggerFactory.getLogger(BalanceFacadeAcceptorSEListener.class);

    private BalanceContext balanceContext;

    private BalanceRouter  balanceRouter;

    public BalanceFacadeAcceptorSEListener(BalanceContext balanceContext) {
        this.balanceContext = balanceContext;
        this.balanceRouter = balanceContext.getBalanceRouter();
    }

    @Override
    public void sessionOpened(SocketSession session) {
        balanceRouter.addClientSession((BalanceFacadeSocketSession) session);
        logger.info("client from [ {} ] connected.", session.getRemoteSocketAddress());
    }

    @Override
    public void sessionClosed(SocketSession session) {

        BalanceFacadeSocketSession fs = (BalanceFacadeSocketSession) session;

        balanceRouter.removeClientSession(fs);

        logger.info("client from [ {} ] disconnected.", session.getRemoteSocketAddress());

        BalanceRouter balanceRouter = balanceContext.getBalanceRouter();

        BalanceReverseSocketSession rs = balanceRouter.getRouterSession(fs);

        if (rs == null) {
            return;
        }

        ChannelLostFutureFactory factory = balanceContext.getChannelLostReadFutureFactory();

        if (factory == null) {
            return;
        }

        rs.flush(factory.createChannelLostPacket(fs));
    }
}
