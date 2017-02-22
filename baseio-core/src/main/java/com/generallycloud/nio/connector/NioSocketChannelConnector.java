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
package com.generallycloud.nio.connector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.LoggerUtil;
import com.generallycloud.nio.component.NioChannelService;
import com.generallycloud.nio.component.NioSocketChannelContext;
import com.generallycloud.nio.component.SelectorEventLoopGroup;
import com.generallycloud.nio.component.SocketSelectorBuilder;
import com.generallycloud.nio.component.SocketSelectorEventLoopGroup;
import com.generallycloud.nio.component.UnsafeSocketSession;
import com.generallycloud.nio.configuration.ServerConfiguration;

/**
 * @author wangkai
 *
 */
public class NioSocketChannelConnector extends AbstractSocketChannelConnector
		implements NioChannelService {

	private NioSocketChannelContext	context;

	private SelectableChannel		selectableChannel		= null;

	private SocketSelectorBuilder		selectorBuilder		= null;

	private SelectorEventLoopGroup	selectorEventLoopGroup	= null;

	private Logger					logger				= LoggerFactory
			.getLogger(getClass());

	//FIXME 优化
	protected NioSocketChannelConnector(NioSocketChannelContext context) {
		this.selectorBuilder = new ClientNioSocketSelectorBuilder(this);
		this.context = context;
	}

	@Override
	protected void destroyService() {
		CloseUtil.close(selectableChannel);
		LifeCycleUtil.stop(selectorEventLoopGroup);
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
	protected void connect(InetSocketAddress socketAddress) throws IOException {

		this.initChannel();

		((SocketChannel) this.selectableChannel).connect(socketAddress);

		this.initSelectorLoops();

		wait4connect();
	}

	protected void finishConnect(UnsafeSocketSession session, Exception exception) {

		if (exception == null) {

			this.session = session;

			LoggerUtil.prettyNIOServerLog(logger, "connected to server @{}", getServerSocketAddress());

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
	public NioSocketChannelContext getContext() {
		return context;
	}

	private void initChannel() throws IOException {
		this.selectableChannel = SocketChannel.open();
		this.selectableChannel.configureBlocking(false);
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
