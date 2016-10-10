package com.generallycloud.nio.component;

import java.io.IOException;
import java.nio.channels.SelectionKey;

public class SocketChannelSelectionWriter implements SelectionAcceptor {

	public void accept(SelectionKey selectionKey) throws IOException {

		SocketChannel channel = (SocketChannel) selectionKey.attachment();

		if (!channel.isOpened()) {
			return;
		}

		channel.wakeup();
	}

}
