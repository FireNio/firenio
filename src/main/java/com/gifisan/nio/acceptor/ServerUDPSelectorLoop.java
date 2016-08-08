package com.gifisan.nio.acceptor;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.component.UDPSelectorLoop;

public class ServerUDPSelectorLoop extends UDPSelectorLoop {

	public ServerUDPSelectorLoop(NIOContext context) {
		super(context);

	}

	public void register(NIOContext context, SelectableChannel channel) throws IOException {
		// 打开selector
		this.selector = Selector.open();
		// 注册监听事件到该selector
		channel.register(selector, SelectionKey.OP_READ);
	}

}
