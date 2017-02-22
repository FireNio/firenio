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
package com.generallycloud.nio.front;

import com.generallycloud.nio.balance.BalanceReadFuture;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.IoEventHandleAdaptor;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.ReadFuture;

public class BalanceFacadeConnectorHandler extends IoEventHandleAdaptor {

	private Logger				logger	= LoggerFactory.getLogger(BalanceFacadeConnectorHandler.class);
	private FrontRouter			frontRouter;
	private FrontFacadeAcceptor	frontFacadeAcceptor;

	public BalanceFacadeConnectorHandler(FrontContext frontContext) {
		this.frontRouter = frontContext.getFrontRouter();
		this.frontFacadeAcceptor = frontContext.getFrontFacadeAcceptor();
	}

	@Override
	public void accept(SocketSession session, ReadFuture future) throws Exception {

		BalanceReadFuture f = (BalanceReadFuture) future;

		if (f.isBroadcast()) {

			frontFacadeAcceptor.getAcceptor().broadcast(f.translate());

			logger.info("broadcast msg, F: {}, msg: {}", session.getRemoteSocketAddress(), f);

			return;
		}

		//FIXME 将来考虑是否实现
//		int sessionID = f.getClientSessionID();

		//FIXME 这行代码是错误的
		int sessionID = f.getSessionID();
		
		SocketSession response = frontRouter.getClientSession(sessionID);

		if (response == null || response.isClosed()) {
			logger.info("none load node found: [ {} ], msg: {}", response.getRemoteSocketAddress(), f);
			return;

		}

		response.flush(f.translate());

		logger.info("dispatch msg: F:[ {} ],T:[ {} ], msg :{}", 
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
