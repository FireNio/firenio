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

import com.generallycloud.nio.component.AbstractSessionManager;
import com.generallycloud.nio.component.ChannelService;
import com.generallycloud.nio.component.Selector;
import com.generallycloud.nio.component.SelectorEventLoop;
import com.generallycloud.nio.component.SelectorEventLoopGroup;
import com.generallycloud.nio.component.SocketChannelSelectorLoop;

public class ServerSocketChannelSelectorLoop extends SocketChannelSelectorLoop {

	public ServerSocketChannelSelectorLoop(ChannelService service, SelectorEventLoopGroup selectorEventLoopGroup) {
		super(service, selectorEventLoopGroup);
	}

	@Override
	public Selector buildSelector(SelectableChannel channel) throws IOException {

		// 打开selector
		java.nio.channels.Selector selector = java.nio.channels.Selector.open();
		
		SelectorEventLoop[] selectorLoops = selectorEventLoopGroup.getSelectorEventLoops();

		if (selectorLoops[0] == this) {

			// 注册监听事件到该selector
			channel.register(selector, SelectionKey.OP_ACCEPT);

			this.setMainSelector(true);

			AbstractSessionManager sessionManager = (AbstractSessionManager) this.context.getSessionManager();

			sessionManager.initSessionManager(this);

			return new ServerNioSelector(this, selector, channel, selectorEventLoopGroup);
		}

		return new ServerNioSelector(this, selector, channel, selectorEventLoopGroup);
	}

}
