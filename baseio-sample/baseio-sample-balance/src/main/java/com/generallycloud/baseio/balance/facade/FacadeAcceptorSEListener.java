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
import com.generallycloud.baseio.balance.reverse.ReverseSocketChannel;
import com.generallycloud.baseio.balance.router.BalanceRouter;
import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.component.ChannelEventListenerAdapter;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public class FacadeAcceptorSEListener extends ChannelEventListenerAdapter {

    private Logger         logger = LoggerFactory.getLogger(getClass());

    private BalanceContext context;

    public FacadeAcceptorSEListener(BalanceContext context) {
        this.context = context;
    }

    @Override
    public void sessionOpened(NioSocketChannel channel) {
        BalanceRouter balanceRouter = context.getBalanceRouter();
        balanceRouter.addClientChannel((FacadeSocketChannel) channel);
        logger.info("client from [ {}:{} ] connected.", channel.getRemoteAddr(),
                channel.getRemotePort());
    }

    @Override
    public void sessionClosed(NioSocketChannel channel) {
        BalanceRouter balanceRouter = context.getBalanceRouter();
        FacadeSocketChannel fs = (FacadeSocketChannel) channel;
        balanceRouter.removeClientChannel(fs);
        logger.info("client from [ {}:{} ] disconnected.", channel.getRemoteAddr(),
                channel.getRemotePort());
        ReverseSocketChannel rs = balanceRouter.getRouterChannel(fs);
        if (rs == null) {
            return;
        }
        ChannelLostFutureFactory factory = context.getChannelLostReadFutureFactory();
        if (factory == null) {
            return;
        }
        rs.flush(factory.createChannelLostPacket(fs));
    }

}
