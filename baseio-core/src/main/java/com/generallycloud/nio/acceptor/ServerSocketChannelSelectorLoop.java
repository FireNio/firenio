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

import com.generallycloud.nio.component.ChannelService;
import com.generallycloud.nio.component.MinorSelectorLoopStrategy;
import com.generallycloud.nio.component.PrimarySelectorLoopStrategy;
import com.generallycloud.nio.component.SelectorLoop;
import com.generallycloud.nio.component.SocketChannel;
import com.generallycloud.nio.component.SocketChannelSelectorLoop;
import com.generallycloud.nio.component.concurrent.FixedAtomicInteger;

public class ServerSocketChannelSelectorLoop extends SocketChannelSelectorLoop {
	
	private FixedAtomicInteger	core_index;

	public ServerSocketChannelSelectorLoop(ChannelService service, SelectorLoop[] selectorLoops) {
		super(service,selectorLoops);
	}

	@Override
	public Selector buildSelector(SelectableChannel channel) throws IOException {
		
		// 打开selector
		Selector selector = Selector.open();

		if (selectorLoops[0] == this) {
			
			// 注册监听事件到该selector
			channel.register(selector, SelectionKey.OP_ACCEPT);
			
			this.setMainSelector(true);
			
			this.core_index = new FixedAtomicInteger(selectorLoops.length -1);
			
			this.selectorLoopStrategy = new PrimarySelectorLoopStrategy(this);
			
			return selector;
		}
		
		this.selectorLoopStrategy = new MinorSelectorLoopStrategy(this);

		return selector;
	}

	@Override
	protected void acceptPrepare(SelectionKey selectionKey) throws IOException {

		ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();
		
		java.nio.channels.SocketChannel channel = server.accept();

		if (channel == null) {
			return;
		}

		int next_core_index = core_index.getAndIncrement();
		
		SelectorLoop selectorLoop = selectorLoops[next_core_index];

		// 配置为非阻塞
		channel.configureBlocking(false);
		
		// 注册到selector，等待连接
		if (selectorLoop.isMainSelector()) {
			regist(channel, selectorLoop);
			return;
		}
		
		ReentrantLock lock = selectorLoop.getIsWaitForRegistLock();
		
		lock.lock();
		
		try{
			
			selectorLoop.setWaitForRegist(true);

			selectorLoop.wakeup();

			regist(channel, selectorLoop);
			
			selectorLoop.setWaitForRegist(false);
			
		}finally{
			
			lock.unlock();
		}
		
	}
	
	private void regist(java.nio.channels.SocketChannel channel,SelectorLoop selectorLoop) throws IOException{

		SelectionKey sk = channel.register(selectorLoop.getSelector(), SelectionKey.OP_READ);
		
		// 绑定SocketChannel到SelectionKey
		SocketChannel socketChannel = selectorLoop.buildSocketChannel(sk);

		// fire session open event
		socketChannel.getSession().fireOpend();
		// logger.debug("__________________chanel____gen____{}", channel);
	}

}
