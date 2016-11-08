package com.generallycloud.nio.connector;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import com.generallycloud.nio.component.SocketChannelSelectorLoop;

public class ClientTCPSelectorLoop extends SocketChannelSelectorLoop {

	public ClientTCPSelectorLoop(SocketChannelConnector connector) {

		super(connector.getContext(),connector.getSelectableChannel());

		this._alpha_acceptor = new SocketChannelSelectionConnector(this, connector);
	}

	public void register(SelectableChannel channel) throws IOException {
	}

	//FIXME open channel
	public Selector buildSelector(SelectableChannel channel) throws IOException {
		
		Selector selector = Selector.open();
		
		channel.register(selector, SelectionKey.OP_CONNECT);
		
		return selector;
	}
}
