package com.generallycloud.nio.connector;

import java.io.IOException;
import java.nio.channels.SelectionKey;

import com.generallycloud.nio.component.AbstractTCPSelectionAlpha;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.SelectorLoop;
import com.generallycloud.nio.component.SocketChannel;

public class SocketChannelSelectionConnector extends AbstractTCPSelectionAlpha {

	private SocketChannelConnector	connector;

	private SelectorLoop			selectorLoop;

	public SocketChannelSelectionConnector(SelectorLoop selectorLoop, SocketChannelConnector connector) {
		super(connector.getContext());
		this.connector = connector;
		this.selectorLoop = selectorLoop;
	}

	public void accept(SelectionKey selectionKey) throws Exception {

		java.nio.channels.SocketChannel channel = (java.nio.channels.SocketChannel) selectionKey.channel();

		// does it need connection pending ?
		if (!channel.isConnectionPending()) {

			return;
		}

		finishConnect(selectionKey, channel);
	}

	private void finishConnect(SelectionKey selectionKey, java.nio.channels.SocketChannel channel) {

		BaseContext context = selectorLoop.getContext();

		try {

			channel.finishConnect();

			channel.register(selectorLoop.getSelector(), SelectionKey.OP_READ);

			final SocketChannel socketChannel = attachSocketChannel(selectionKey, selectorLoop);

			socketChannel.getSession().getEventLoop().dispatch(new Runnable() {

				public void run() {
					connector.finishConnect(socketChannel.getSession(), null);
				}
			});
		} catch (final IOException e) {

			context.getEventLoopGroup().getNext().dispatch(new Runnable() {

				public void run() {
					connector.finishConnect(null, e);
				}
			});
		} catch (final Exception e) {

			context.getEventLoopGroup().getNext().dispatch(new Runnable() {

				public void run() {
					connector.finishConnect(null, new IOException(e.getMessage(), e));
				}
			});
		}
	}

}
