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
import java.nio.channels.Selector;

import com.generallycloud.nio.component.NioSelector;
import com.generallycloud.nio.component.SocketChannel;
import com.generallycloud.nio.component.SocketChannelSelectorLoop;

/**
 * @author wangkai
 *
 */
public class ClientNioSelector extends NioSelector {

	public ClientNioSelector(SocketChannelSelectorLoop selectorEventLoop, Selector selector,
			SelectableChannel selectableChannel, SocketChannelConnector connector) {
		super(selectorEventLoop, selector, selectableChannel);
		this.connector = connector;
	}

	private SocketChannelConnector connector;

	protected void buildChannel(SelectionKey selectionKey) throws IOException {

		java.nio.channels.SocketChannel channel = (java.nio.channels.SocketChannel) selectableChannel;

		// does it need connection pending ?
		if (!channel.isConnectionPending()) {
			throw new IOException("connection is pending");
		}

		finishConnect(selectionKey, channel);
	}

	private void finishConnect(SelectionKey selectionKey, java.nio.channels.SocketChannel channel)
			throws IOException {

		try {

			channel.finishConnect();

			channel.register(getSelector(), SelectionKey.OP_READ);

			SocketChannel socketChannel = buildSocketChannel(selectionKey);

			connector.finishConnect(socketChannel.getSession(), null);

		} catch (IOException e) {

			connector.finishConnect(null, e);

			throw e;
		}
	}

}
