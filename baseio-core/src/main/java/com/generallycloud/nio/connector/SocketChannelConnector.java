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
package com.generallycloud.nio.connector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import com.generallycloud.nio.TimeoutException;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.MessageFormatter;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.component.UnsafeSocketSession;
import com.generallycloud.nio.component.concurrent.Waiter;

public final class SocketChannelConnector extends AbstractChannelConnector {

	private SocketChannelContext	context;

	private UnsafeSocketSession	session;

	private Waiter<Object> waiter;

	//FIXME 优化
	public SocketChannelConnector(SocketChannelContext context) {
		this.selectorBuilder = new ClientNioSelectorBuilder(this);
		this.context = context;
	}

	@Override
	public SocketSession connect() throws IOException {

		this.waiter = new Waiter<Object>();
		
		this.session = null;
		
		this.service();

		return getSession();
	}

	@Override
	protected void connect(InetSocketAddress socketAddress) throws IOException {

		((SocketChannel) this.selectableChannel).connect(socketAddress);

		initSelectorLoops();

		if (waiter.await(getTimeout())) {

			CloseUtil.close(this);

			throw new TimeoutException("connect to " + socketAddress.toString() + " time out");
		}

		Object o = waiter.getPayload();

		if (o instanceof Exception) {
			
			CloseUtil.close(this);

			Exception t = (Exception) o;

			throw new TimeoutException(MessageFormatter.format("connect faild,connector:[{}],nested exception is {}",
					socketAddress, t.getMessage()), t);
		}
	}

	protected void finishConnect(UnsafeSocketSession session, Exception exception) {

		if (exception == null) {

			this.session = session;
			
			fireSessionOpend();

			this.waiter.setPayload(null);

			if (waiter.isTimeouted()) {
				CloseUtil.close(this);
			}
		} else {

			this.waiter.setPayload(exception);
		}
	}
	
	@Override
	protected boolean canSafeClose() {
		return session == null || (!session.inSelectorLoop() && !session.getExecutorEventLoop().inEventLoop());
	}

	@Override
	protected void fireSessionOpend() {
		session.fireOpend();
	}

	@Override
	public SocketChannelContext getContext() {
		return context;
	}

	@Override
	public SocketSession getSession() {
		return session;
	}

	@Override
	protected void initselectableChannel() throws IOException {

		this.selectableChannel = SocketChannel.open();

		this.selectableChannel.configureBlocking(false);
	}

}
