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
import java.net.BindException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;

import com.generallycloud.nio.component.DatagramChannelContext;
import com.generallycloud.nio.component.DatagramChannelSELFactory;
import com.generallycloud.nio.protocol.ReadFuture;

public final class DatagramChannelAcceptor extends AbstractChannelAcceptor {

	private DatagramChannelContext	context		= null;

	private DatagramSocket			datagramSocket	= null;

	public DatagramChannelAcceptor(DatagramChannelContext context) {
		this.context = context;
	}

	@Override
	protected void bind(InetSocketAddress socketAddress) throws IOException {

		try {
			// 进行服务的绑定
			datagramSocket.bind(socketAddress);
		} catch (BindException e) {
			throw new BindException(e.getMessage() + " at " + socketAddress.getPort());
		}

		initSelectorLoops(new DatagramChannelSELFactory(this));
	}

	@Override
	public void broadcast(final ReadFuture future) {
		throw new UnsupportedOperationException();
	}

	@Override
	public DatagramChannelContext getContext() {
		return context;
	}

	@Override
	protected void initselectableChannel() throws IOException {
		// 打开服务器套接字通道
		this.selectableChannel = DatagramChannel.open();
		// 服务器配置为非阻塞
		this.selectableChannel.configureBlocking(false);

		this.datagramSocket = ((DatagramChannel) this.selectableChannel).socket();
	}

}
