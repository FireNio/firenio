package com.generallycloud.nio.connector;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import com.generallycloud.nio.component.AbstractTCPSelectionAlpha;
import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.SocketChannel;

public class TCPSelectionConnector extends AbstractTCPSelectionAlpha {

	private Selector		selector;
	private TCPConnector	connector;
	private NIOContext		context;

	public TCPSelectionConnector(NIOContext context, TCPConnector connector) {
		super(context);
		this.connector = connector;
		this.context = context;
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

		try {

			channel.finishConnect();

			channel.register(selector, SelectionKey.OP_READ);

			final SocketChannel endPoint = attachEndPoint(context, getChannelWriter(), selectionKey);

			endPoint.getSession().getEventLoop().dispatch(new Runnable() {

				public void run() {
					connector.finishConnect(endPoint.getSession(), null);
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

	protected void setSelector(Selector selector) {
		this.selector = selector;
	}
	
}
