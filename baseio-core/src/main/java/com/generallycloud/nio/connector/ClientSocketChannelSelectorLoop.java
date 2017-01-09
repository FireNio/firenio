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
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;

import com.generallycloud.nio.component.Selector;
import com.generallycloud.nio.component.SelectorEventLoopGroup;
import com.generallycloud.nio.component.SocketChannelSelectorLoop;

public class ClientSocketChannelSelectorLoop extends SocketChannelSelectorLoop {

	private SocketChannelConnector connector;

	public ClientSocketChannelSelectorLoop(SocketChannelConnector connector, SelectorEventLoopGroup group) {

		super(connector, group);

		this.connector = connector;

		this.setMainSelector(true);
	}

	// FIXME open channel
	@Override
	public Selector buildSelector(SelectableChannel channel) throws IOException {

		java.nio.channels.Selector selector = java.nio.channels.Selector.open();

		channel.register(selector, SelectionKey.OP_CONNECT);

		return new ClientNioSelector(this, selector, channel, connector);
	}
}
