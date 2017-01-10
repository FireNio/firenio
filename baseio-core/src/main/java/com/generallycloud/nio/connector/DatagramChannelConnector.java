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

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.LoggerUtil;
import com.generallycloud.nio.component.DatagramChannel;
import com.generallycloud.nio.component.DatagramChannelContext;
import com.generallycloud.nio.component.DatagramSelectorEventLoopImpl;
import com.generallycloud.nio.component.DatagramSession;
import com.generallycloud.nio.component.NioDatagramChannel;
import com.generallycloud.nio.component.UnsafeDatagramSession;
import com.generallycloud.nio.protocol.DatagramPacket;

public final class DatagramChannelConnector extends AbstractChannelConnector {

	private DatagramChannelContext	context	= null;
	private UnsafeDatagramSession		session	= null;
	private Logger 				logger	= LoggerFactory.getLogger(getClass());

	public DatagramChannelConnector(DatagramChannelContext context) {
		this.context = context;
	}

	@Override
	public DatagramSession connect() throws IOException {

		this.session = null;
		
		this.service();

		return getSession();
	}

	@Override
	protected void connect(InetSocketAddress socketAddress) throws IOException {

		((java.nio.channels.DatagramChannel) this.selectableChannel).connect(socketAddress);

		initSelectorLoops();
		
		DatagramSelectorEventLoopImpl selectorLoop = (DatagramSelectorEventLoopImpl) selectorEventLoopGroup.getNext();

		@SuppressWarnings("resource")
		DatagramChannel channel = new NioDatagramChannel(selectorLoop,
				(java.nio.channels.DatagramChannel) selectableChannel, socketAddress);
		
		this.session = channel.getSession();
		
		LoggerUtil.prettyNIOServerLog(logger, "已连接到远程服务器 @{}",getServerSocketAddress());
	}
	
	@Override
	protected boolean canSafeClose() {
		return session == null || !session.inSelectorLoop();
	}

	@Override
	protected void fireSessionOpend() {
		session.fireOpend();
	}

	@Override
	public DatagramChannelContext getContext() {
		return context;
	}

	@Override
	public DatagramSession getSession() {
		return session;
	}

	@Override
	protected void initselectableChannel() throws IOException {

		this.selectableChannel = java.nio.channels.DatagramChannel.open();

		this.selectableChannel.configureBlocking(false);
	}

	public void sendDatagramPacket(DatagramPacket packet) throws IOException {
		session.sendPacket(packet);
	}

}
