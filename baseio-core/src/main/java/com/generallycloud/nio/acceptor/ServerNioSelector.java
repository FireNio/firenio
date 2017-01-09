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
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.component.NioSelector;
import com.generallycloud.nio.component.SelectorEventLoop;
import com.generallycloud.nio.component.SelectorEventLoopGroup;
import com.generallycloud.nio.component.SocketChannel;
import com.generallycloud.nio.component.SocketChannelSelectorLoop;

/**
 * @author wangkai
 *
 */
public class ServerNioSelector extends NioSelector {

	private SelectorEventLoopGroup selectorEventLoopGroup;

	public ServerNioSelector(SocketChannelSelectorLoop selectorEventLoop, Selector selector,
			SelectableChannel selectableChannel, SelectorEventLoopGroup selectorEventLoopGroup) {
		super(selectorEventLoop, selector, selectableChannel);
		this.selectorEventLoopGroup = selectorEventLoopGroup;
	}

	@Override
	protected void buildChannel(SelectionKey k) throws IOException {

		java.nio.channels.SocketChannel channel = ((ServerSocketChannel) selectableChannel).accept();

		SelectorEventLoop selectorLoop = selectorEventLoopGroup.getNext();

		// 配置为非阻塞
		channel.configureBlocking(false);

		// 注册到selector，等待连接
		if (selectorLoop.isMainSelector()) {
			regist(channel, selectorLoop);
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

	private void regist(java.nio.channels.SocketChannel channel, SelectorEventLoop selectorLoop) throws IOException {

		NioSelector nioSelector = (NioSelector) selectorLoop.getSelector();

		SelectionKey sk = channel.register(nioSelector.getSelector(), SelectionKey.OP_READ);

		// 绑定SocketChannel到SelectionKey
		SocketChannel socketChannel = buildSocketChannel(sk);
		
		// fire session open event 
		socketChannel.getSession().fireOpend();
	}

}
