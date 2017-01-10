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
package com.generallycloud.nio.acceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;

import com.generallycloud.nio.TimeoutException;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.LoggerUtil;
import com.generallycloud.nio.component.AbstractChannelService;
import com.generallycloud.nio.component.concurrent.Waiter;
import com.generallycloud.nio.configuration.ServerConfiguration;

public abstract class AbstractChannelAcceptor extends AbstractChannelService implements NioChannelAcceptor {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void bind() throws IOException {
		service();
	}

	@Override
	protected void initService(ServerConfiguration configuration) throws IOException {

		this.serverAddress = new InetSocketAddress(configuration.getSERVER_PORT());

		this.bind(getServerSocketAddress());

		LoggerUtil.prettyNIOServerLog(logger, "监听已启动 @{}", getServerSocketAddress());
	}

	protected abstract void bind(InetSocketAddress socketAddress) throws IOException;

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public void unbind() throws TimeoutException {

		Waiter<IOException> waiter = asynchronousUnbind();

		if (waiter.await()) {
			// FIXME never timeout
			throw new TimeoutException("timeout to unbind");
		}
	}

	@Override
	public Waiter<IOException> asynchronousUnbind() {
		cancelService();
		return shutDownWaiter;
	}

	@Override
	public int getManagedSessionSize() {
		return getContext().getSessionManager().getManagedSessionSize();
	}

	@Override
	public SelectableChannel getSelectableChannel() {
		return selectableChannel;
	}
}
