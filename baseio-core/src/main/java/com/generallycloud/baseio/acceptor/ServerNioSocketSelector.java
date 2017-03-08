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
package com.generallycloud.baseio.acceptor;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.baseio.component.NioSocketChannel;
import com.generallycloud.baseio.component.NioSocketSelector;
import com.generallycloud.baseio.component.SocketSelectorEventLoop;
import com.generallycloud.baseio.component.SocketSelectorEventLoopGroup;

/**
 * @author wangkai
 *
 */
public class ServerNioSocketSelector extends NioSocketSelector {

	private SocketSelectorEventLoopGroup selectorEventLoopGroup;

	public ServerNioSocketSelector(SocketSelectorEventLoop loop, Selector selector,
			SelectableChannel channel, SocketSelectorEventLoopGroup group) {
		super(loop, selector, channel);
		this.selectorEventLoopGroup = group;
	}

	@Override
	protected void buildChannel(SelectionKey k) throws IOException {

		ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectableChannel;

		java.nio.channels.SocketChannel channel = serverSocketChannel.accept();

		SocketSelectorEventLoop selectorLoop = selectorEventLoopGroup.getNext();

		// 配置为非阻塞
		channel.configureBlocking(false);

		// 注册到selector，等待连接
		if (selectorLoop.isMainEventLoop()) {
			regist(channel, selectorLoop);
			return;
		}

		ReentrantLock lock = selectorLoop.getIsWaitForRegistLock();

		lock.lock();

		try {

			selectorLoop.setWaitForRegist(true);

			selectorLoop.wakeup();

			regist(channel, selectorLoop);

		} finally {

			selectorLoop.setWaitForRegist(false);

			lock.unlock();
		}
	}

	private void regist(java.nio.channels.SocketChannel channel,
			SocketSelectorEventLoop selectorLoop) throws IOException {

		NioSocketSelector nioSelector = (NioSocketSelector) selectorLoop.getSelector();

		SelectionKey sk = channel.register(nioSelector.getSelector(), SelectionKey.OP_READ);

		// 绑定SocketChannel到SelectionKey
		NioSocketChannel socketChannel = newChannel(sk, selectorLoop);

		// fire session open event
		socketChannel.fireOpend();
	}

}
