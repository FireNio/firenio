package com.generallycloud.nio.connector;

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import com.generallycloud.nio.component.PrimarySelectorLoopStrategy;
import com.generallycloud.nio.component.SelectorLoop;
import com.generallycloud.nio.component.SocketChannel;
import com.generallycloud.nio.component.SocketChannelSelectorLoop;

public class ClientSocketChannelSelectorLoop extends SocketChannelSelectorLoop {
	
	private SocketChannelConnector	connector;

	public ClientSocketChannelSelectorLoop(SocketChannelConnector connector, SelectorLoop[] selectorLoops) {

		super(connector,selectorLoops);
		
		this.connector = connector;

		this.setMainSelector(true);
		
		this.selectorLoopStrategy = new PrimarySelectorLoopStrategy(context);
	}

	//FIXME open channel
	public Selector buildSelector(SelectableChannel channel) throws IOException {
		
		Selector selector = Selector.open();
		
		channel.register(selector, SelectionKey.OP_CONNECT);
		
		return selector;
	}

	@Override
	protected void acceptPrepare(SelectionKey selectionKey) throws IOException {
		
		java.nio.channels.SocketChannel channel = (java.nio.channels.SocketChannel) selectionKey.channel();

		// does it need connection pending ?
		if (!channel.isConnectionPending()) {

			return;
		}

		finishConnect(selectionKey, channel);
		
	}
	
	private void finishConnect(SelectionKey selectionKey, java.nio.channels.SocketChannel channel) {

		try {

			channel.finishConnect();

			channel.register(getSelector(), SelectionKey.OP_READ);

			SocketChannel socketChannel = selectorLoopStrategy.buildSocketChannel(selectionKey, this);
			
			connector.finishConnect(socketChannel.getSession(), null);

		} catch (IOException e) {
			
			connector.finishConnect(null, e);

		} catch (Exception e) {
			
			connector.finishConnect(null, new IOException(e.getMessage(), e));
		}
	}
}
