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
package com.generallycloud.baseio.connector;

import java.io.IOException;

import com.generallycloud.baseio.TimeoutException;
import com.generallycloud.baseio.common.CloseUtil;
import com.generallycloud.baseio.common.Logger;
import com.generallycloud.baseio.common.LoggerUtil;
import com.generallycloud.baseio.common.MessageFormatter;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.component.UnsafeSocketSession;
import com.generallycloud.baseio.concurrent.Waiter;

/**
 * @author wangkai
 *
 */
public abstract class AbstractSocketChannelConnector extends AbstractChannelConnector {
	
	protected UnsafeSocketSession	session;

	protected Waiter<Object>		waiter;

	@Override
	public SocketSession getSession() {
		return session;
	}

	@Override
	protected boolean canSafeClose() {
		return session == null
				|| (!session.inSelectorLoop() && !session.getExecutorEventLoop().inEventLoop());
	}
	
	//FIXME protected
	public void finishConnect(UnsafeSocketSession session, Throwable exception) {
		
		if (exception == null) {

			this.session = session;
			
			LoggerUtil.prettyLog(getLogger(), "connected to server @{}", getServerSocketAddress());

			this.waiter.setPayload(null);
			
			if (waiter.isTimeouted()) {
				CloseUtil.close(this);
			}
			
		} else {

			this.waiter.setPayload(exception);
		}
	}

	@Override
	public SocketSession connect() throws IOException {

		this.waiter = new Waiter<Object>();

		this.session = null;

		this.initialize();

		return getSession();
	}

	protected void wait4connect() throws TimeoutException {

		if (waiter.await(getTimeout())) {

			CloseUtil.close(this);

			throw new TimeoutException(
					"connect to " + getServerSocketAddress().toString() + " time out");
		}

		Object o = waiter.getPayload();

		if (o instanceof Exception) {

			CloseUtil.close(this);

			Exception t = (Exception) o;

			throw new TimeoutException(MessageFormatter.format(
					"connect faild,connector:[{}],nested exception is {}",
					getServerSocketAddress(), t.getMessage()), t);
		}
	}
	
	abstract Logger getLogger();

}
