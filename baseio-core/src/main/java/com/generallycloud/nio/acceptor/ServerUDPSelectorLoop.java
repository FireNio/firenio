package com.generallycloud.nio.acceptor;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.DatagramChannelSelectorLoop;

public class ServerUDPSelectorLoop extends DatagramChannelSelectorLoop {

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
