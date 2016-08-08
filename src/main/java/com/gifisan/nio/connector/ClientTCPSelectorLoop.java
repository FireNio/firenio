package com.gifisan.nio.connector;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import com.gifisan.nio.component.EndPointWriter;
import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.component.TCPSelectorLoop;

public class ClientTCPSelectorLoop extends TCPSelectorLoop {

	public ClientTCPSelectorLoop(NIOContext context, TCPConnector connector, EndPointWriter endPointWriter) {

		super(context, endPointWriter);

		this._alpha_acceptor = new TCPSelectionConnector(context, connector, endPointWriter);
	}

	public void register(NIOContext context, SelectableChannel channel) throws IOException {

		this.selector = Selector.open();

		TCPSelectionConnector selectionConnector = (TCPSelectionConnector) this._alpha_acceptor;

		selectionConnector.setSelector(selector);

		channel.register(selector, SelectionKey.OP_CONNECT);
	}

}
