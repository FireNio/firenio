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
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;

import com.generallycloud.nio.component.AbstractSessionManager;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketSelector;
import com.generallycloud.nio.component.SocketSelectorBuilder;
import com.generallycloud.nio.component.SocketSelectorEventLoop;

/**
 * @author wangkai
 *
 */
public class ServerNioSocketSelectorBuilder implements SocketSelectorBuilder{

	@Override
	public SocketSelector build(SocketSelectorEventLoop selectorLoop) throws IOException {
		
		SocketChannelContext context = selectorLoop.getChannelContext();
		
		NioChannelAcceptor nioChannelAcceptor = (NioChannelAcceptor) context.getChannelService();
		
		ServerSocketChannel channel = (ServerSocketChannel)nioChannelAcceptor.getSelectableChannel();
		
		// 打开selector
		java.nio.channels.Selector selector = java.nio.channels.Selector.open();
		
		if (selectorLoop.isMainEventLoop()) {

			// 注册监听事件到该selector
			channel.register(selector, SelectionKey.OP_ACCEPT);

			AbstractSessionManager sessionManager = (AbstractSessionManager) context.getSessionManager();

			sessionManager.initSessionManager(selectorLoop);

			return new ServerNioSocketSelector(selectorLoop, selector, channel, selectorLoop.getEventLoopGroup());
		}

		return new ServerNioSocketSelector(selectorLoop, selector, channel, selectorLoop.getEventLoopGroup());
	}
	
}
