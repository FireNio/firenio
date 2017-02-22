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
package com.generallycloud.nio.acceptor;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.component.NioChannelService;
import com.generallycloud.nio.component.NioSocketChannelContext;
import com.generallycloud.nio.component.SelectorEventLoopGroup;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketSelectorBuilder;
import com.generallycloud.nio.component.SocketSelectorEventLoopGroup;
import com.generallycloud.nio.configuration.ServerConfiguration;

/**
 * @author wangkai
 *
 */
public class NioSocketChannelAcceptor extends AbstractSocketChannelAcceptor
		implements NioChannelService {

	private ServerSocket			serverSocket			= null;

	private SelectableChannel		selectableChannel		= null;

	private SocketSelectorBuilder		selectorBuilder		= null;

	private SelectorEventLoopGroup	selectorEventLoopGroup	= null;

	public NioSocketChannelAcceptor(SocketChannelContext context) {
		super(context);
		this.selectorBuilder = new ServerNioSocketSelectorBuilder();
	}

	@Override
	protected void bind(InetSocketAddress socketAddress) throws IOException {

		initChannel();

		try {
			// 进行服务的绑定
			this.serverSocket.bind(socketAddress, 50);
		} catch (BindException e) {
			throw new BindException(e.getMessage() + " at " + socketAddress.getPort());
		}

		initSelectorLoops();
	}

	private void initChannel() throws IOException {
		// 打开服务器套接字通道
		this.selectableChannel = ServerSocketChannel.open();
		// 服务器配置为非阻塞
		this.selectableChannel.configureBlocking(false);
		// 检索与此通道关联的服务器套接字
		this.serverSocket = ((ServerSocketChannel) selectableChannel).socket();
	}

	private void initSelectorLoops() {

		//FIXME socket selector event loop ?
		ServerConfiguration configuration = getContext().getServerConfiguration();

		int core_size = configuration.getSERVER_CORE_SIZE();

		int eventQueueSize = configuration.getSERVER_IO_EVENT_QUEUE();

		this.selectorEventLoopGroup = new SocketSelectorEventLoopGroup(
				(NioSocketChannelContext) getContext(), "io-process", eventQueueSize,
				core_size);
		LifeCycleUtil.start(selectorEventLoopGroup);
	}

	@Override
	protected void destroyService() {
		CloseUtil.close(serverSocket);
		CloseUtil.close(selectableChannel);
		LifeCycleUtil.stop(selectorEventLoopGroup);
	}

	@Override
	public SocketSelectorBuilder getSelectorBuilder() {
		return selectorBuilder;
	}

	@Override
	public SelectableChannel getSelectableChannel() {
		return selectableChannel;
	}

}
