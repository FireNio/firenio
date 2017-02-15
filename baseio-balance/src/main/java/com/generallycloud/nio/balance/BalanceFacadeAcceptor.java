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

import java.io.IOException;

import com.generallycloud.nio.acceptor.SocketChannelAcceptor;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.LoggerUtil;
import com.generallycloud.nio.component.SocketChannelContext;

public class BalanceFacadeAcceptor {

	private byte[]			runLock			= new byte[] {};
	private boolean			running			= false;
	private BalanceContext		balanceContext		= null;
	private SocketChannelAcceptor	channelAcceptor	= null;

	public void start(BalanceContext balanceContext, SocketChannelContext facadeContext,
			SocketChannelContext reverseContext) throws IOException {

		if (balanceContext == null) {
			throw new IllegalArgumentException("null configuration");
		}
		
		synchronized (runLock) {

			if (running) {
				return;
			}

			this.balanceContext = balanceContext;

			this.balanceContext.getBalanceReverseAcceptor().start(reverseContext);

			this.channelAcceptor = new SocketChannelAcceptor(facadeContext);

			this.channelAcceptor.bind();

			LoggerUtil.prettyNIOServerLog(LoggerFactory.getLogger(BalanceFacadeAcceptor.class),
					"Balance Facade Acceptor 启动成功 ...");
		}

	}

	public void stop() {
		synchronized (runLock) {
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
