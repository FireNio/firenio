package com.gifisan.nio.acceptor;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import com.gifisan.nio.component.EndPointWriter;
import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.component.TCPSelectorLoop;

public class ServerTCPSelectorLoop extends TCPSelectorLoop {

	public ServerTCPSelectorLoop(NIOContext context, EndPointWriter endPointWriter,CoreProcessors processors) {

		super(context, endPointWriter);

		this._alpha_acceptor = new TCPSelectionAcceptor(context, endPointWriter,processors);
	}

	public void register(NIOContext context, SelectableChannel channel) throws IOException {
		// 打开selector
		this.selector = Selector.open();
		
		TCPSelectionAcceptor selectionAcceptor = (TCPSelectionAcceptor) this._alpha_acceptor;
		
		selectionAcceptor.setSelector(selector);
		
		// 注册监听事件到该selector
		channel.register(selector, SelectionKey.OP_ACCEPT);
	}
}
