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
package com.generallycloud.baseio.balance;

import com.generallycloud.baseio.balance.router.BalanceRouter;
import com.generallycloud.baseio.common.Logger;
import com.generallycloud.baseio.common.LoggerFactory;
import com.generallycloud.baseio.component.ExceptionCaughtHandle;
import com.generallycloud.baseio.component.IoEventHandleAdaptor;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.protocol.ReadFuture;

public class BalanceFacadeAcceptorHandler extends IoEventHandleAdaptor {

	private Logger					logger	= LoggerFactory.getLogger(getClass());
	private BalanceRouter			balanceRouter;
	private FacadeInterceptor		facadeInterceptor;
	private ExceptionCaughtHandle		exceptionCaughtHandle;
	private BalanceReadFutureFactory	readFutureFactory;

	public BalanceFacadeAcceptorHandler(BalanceContext context) {
		this.balanceRouter = context.getBalanceRouter();
		this.facadeInterceptor = context.getFacadeInterceptor();
		this.readFutureFactory = context.getBalanceReadFutureFactory();
		this.exceptionCaughtHandle = context.getFacadeExceptionCaughtHandle();
	}

	@Override
	public void accept(SocketSession session, ReadFuture future) throws Exception {

		BalanceFacadeSocketSession fs = (BalanceFacadeSocketSession) session;

		BalanceReadFuture f = (BalanceReadFuture) future;

		if (facadeInterceptor.intercept(fs, f)) {
			logger.info("msg intercepted [ {} ], msg: {}", fs.getRemoteSocketAddress(), f);
			return;
		}

		if (f.getToken().longValue() == 0) {
			fs.flush(readFutureFactory.createTokenPacket(fs));
			return;
		}

		BalanceReverseSocketSession rs = balanceRouter.getRouterSession(fs, f);

		if (rs == null || rs.isClosed()) {
			logger.info("none load node found: [ {} ], msg: {}", fs.getRemoteSocketAddress(), f);
			return;
		}

		rs.flush(f.translate());

		logger.info("dispatch msg: F:[ {} ],T:[ {} ], msg :{}", new Object[] {
				session.getRemoteSocketAddress(), rs.getRemoteSocketAddress(), future });
	}

	@Override
	public void exceptionCaught(SocketSession session, ReadFuture future, Exception cause,
			IoEventState state) {
		exceptionCaughtHandle.exceptionCaught(session, future, cause, state);
	}

}
