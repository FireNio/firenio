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
package com.generallycloud.nio.front;

import com.generallycloud.nio.balance.BalanceReadFuture;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.IoEventHandleAdaptor;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.ReadFuture;

public class FrontFacadeAcceptorHandler extends IoEventHandleAdaptor {

	private Logger					logger	= LoggerFactory.getLogger(FrontFacadeAcceptorHandler.class);
	private FrontInterceptor			frontInterceptor;
	private BalanceFacadeConnector	connector;

	public FrontFacadeAcceptorHandler(FrontContext context) {
		this.connector = context.getBalanceFacadeConnector();
		this.frontInterceptor = context.getFrontInterceptor();
	}

	@Override
	public void accept(SocketSession session, ReadFuture future) throws Exception {

		FrontFacadeSocketSession fs = (FrontFacadeSocketSession) session;

		BalanceReadFuture f = (BalanceReadFuture) future;

		if (frontInterceptor.intercept(fs, f)) {
			logger.info("报文被拦截：[ {} ]，报文：{}", fs.getRemoteSocketAddress(), f);
			return;
		}

		SocketSession rs = connector.getSession();

		if (rs == null) {
			logger.info("无负载节点：[ {} ]，报文：{}", fs.getRemoteSocketAddress(), f);
			return;
		}

		//FIXME 将来考虑是否实现
//		f.setClientSessionID(fs.getSessionID());

		f = f.translate();

		rs.flush(f);

		logger.info("分发报文：F：[ {} ]，T：[ {} ]，报文：{}", new Object[]{
				session.getRemoteSocketAddress(),
				rs.getRemoteSocketAddress(),
				future
				
		});
	}

}
