package com.generallycloud.nio.acceptor;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.SelectorLoop;
import com.generallycloud.nio.component.SocketChannelSelectorLoop;

public class ServerTCPSelectorLoop extends SocketChannelSelectorLoop {

	public ServerTCPSelectorLoop(BaseContext context, SelectorLoop[] loops, SelectableChannel selectableChannel) {

		super(context, selectableChannel);

		this._alpha_acceptor = new SocketChannelSelectionAcceptor(context, loops);
	}

	public Selector buildSelector(SelectableChannel channel) throws IOException {

		// 打开selector
		Selector selector = Selector.open();
		// 注册监听事件到该selector

		if (isMainSelector) {
			channel.register(selector, SelectionKey.OP_ACCEPT);
		}

		return selector;
	}

}
