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
package com.generallycloud.nio.balance;

import com.generallycloud.nio.balance.router.BalanceRouter;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.SocketSessionEventListenerAdapter;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.ReadFuture;

public class BalanceFacadeAcceptorSEListener extends SocketSessionEventListenerAdapter {

	private Logger			logger	= LoggerFactory.getLogger(BalanceFacadeAcceptorSEListener.class);

	private BalanceContext	balanceContext;

	private BalanceRouter		balanceRouter;

	public BalanceFacadeAcceptorSEListener(BalanceContext balanceContext) {
		this.balanceContext = balanceContext;
		this.balanceRouter = balanceContext.getBalanceRouter();
	}

	@Override
	public void sessionOpened(SocketSession session) {
		balanceRouter.addClientSession((BalanceFacadeSocketSession) session);
		logger.info("客户端来自 [ {} ] 已建立连接.",session.getRemoteSocketAddress());
	}

	@Override
	public void sessionClosed(SocketSession session) {
		
		BalanceFacadeSocketSession fs = (BalanceFacadeSocketSession) session;

		balanceRouter.removeClientSession(fs);

		logger.info("客户端来自 [ {} ] 已断开连接.",session.getRemoteSocketAddress());

		BalanceRouter balanceRouter = balanceContext.getBalanceRouter();

		BalanceReverseSocketSession rs = balanceRouter.getRouterSession(fs);

		if (rs == null) {
			return;
		}

		ChannelLostReadFutureFactory factory = balanceContext.getChannelLostReadFutureFactory();

		if (factory == null) {
			return;
		}

		ReadFuture future = factory.createChannelLostPacket(session);

		rs.flush(future);
	}
}
