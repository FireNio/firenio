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
package com.generallycloud.baseio.front;

import java.io.IOException;

import com.generallycloud.baseio.acceptor.SocketChannelAcceptor;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.LoggerFactory;
import com.generallycloud.baseio.common.LoggerUtil;
import com.generallycloud.baseio.component.SocketChannelContext;

public class FrontFacadeAcceptor {

	private byte[]				runLock				= new byte[]{};
	private boolean				running				= false;
	private SocketChannelAcceptor		acceptor				= null;
	private FrontContext			frontContext;

	public void start(FrontContext frontContext, SocketChannelContext socketChannelContext, SocketChannelContext balanceReverseChannelContext)
			throws IOException {

		if (frontContext == null) {
			throw new IllegalArgumentException("null configuration");
		}
		
		synchronized (runLock) {
			
			if (running) {
				return;
			}
			
			this.frontContext = frontContext;

			this.frontContext.getBalanceFacadeConnector().connect(balanceReverseChannelContext);

			this.acceptor = new SocketChannelAcceptor(socketChannelContext);

			this.acceptor.bind();

			LoggerUtil.prettyNIOServerLog(LoggerFactory.getLogger(FrontFacadeAcceptor.class),
					"Balance Facade Acceptor 启动成功 ...");
		}

	}

	public void stop() {
		synchronized (runLock) {
			CloseUtil.close(acceptor);
			CloseUtil.close(frontContext.getBalanceFacadeConnector());
		}
	}

	public FrontContext getBalanceContext() {
		return frontContext;
	}

	public SocketChannelAcceptor getAcceptor() {
		return acceptor;
	}
	
}
