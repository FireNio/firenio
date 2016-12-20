/*
 * Copyright 2015 GenerallyCloud.com
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
import com.generallycloud.nio.component.IoEventHandleAdaptor;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.ReadFuture;

public class BalanceReverseAcceptorHandler extends IoEventHandleAdaptor {

	private Logger				logger	= LoggerFactory.getLogger(BalanceReverseAcceptorHandler.class);
	private BalanceRouter		balanceRouter;
	private BalanceFacadeAcceptor	balanceFacadeAcceptor;

	public BalanceReverseAcceptorHandler(BalanceContext balanceContext) {
		this.balanceRouter = balanceContext.getBalanceRouter();
		this.balanceFacadeAcceptor = balanceContext.getBalanceFacadeAcceptor();
	}

	@Override
	public void accept(SocketSession session, ReadFuture future) throws Exception {

		BalanceReadFuture f = (BalanceReadFuture) future;

		if (f.isBroadcast()) {
			
			balanceFacadeAcceptor.getAcceptor().broadcast(f.translate());

			logger.info("广播报文：F：{}，报文：{}", session.getRemoteSocketAddress(), f);

			return;
		}

		int sessionID = f.getSessionID();

		SocketSession response = balanceRouter.getClientSession(sessionID);

		if (response == null || response.isClosed()) {

			logger.info("连接丢失：F：{}，报文：{}", session.getRemoteSocketAddress(), future);

			return;
		}

		response.flush(f.translate());

		logger.info("回复报文：F：[{}]，T：[{}]，报文：{}",
				new Object[] { session.getRemoteSocketAddress(), response.getRemoteSocketAddress(), f });
	}

	@Override
	public void exceptionCaught(SocketSession session, ReadFuture future, Exception cause, IoEventState state) {

		String msg = future.toString();

		if (msg.length() > 100) {
			msg = msg.substring(0, 100);
		}

		logger.error("exceptionCaught,msg=" + msg, cause);
	}

}
