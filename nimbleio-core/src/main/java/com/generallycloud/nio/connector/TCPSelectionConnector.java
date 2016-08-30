package com.generallycloud.nio.connector;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import com.generallycloud.nio.component.AbstractTCPSelectionAlpha;
import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.TCPEndPoint;

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

		SocketChannel channel = (SocketChannel) selectionKey.channel();

		// does it need connection pending ?
		if (!channel.isConnectionPending()) {

			return;
		}

		finishConnect(selectionKey, channel);
	}

	private void finishConnect(SelectionKey selectionKey, SocketChannel channel) {

		try {

			channel.finishConnect();

			channel.register(selector, SelectionKey.OP_READ);

			final TCPEndPoint endPoint = attachEndPoint(context, getEndPointWriter(), selectionKey);

			context.getThreadPool().dispatch(new Runnable() {

				public void run() {
					connector.finishConnect(endPoint.getSession(), null);
				}
			});
		} catch (final IOException e) {

			context.getThreadPool().dispatch(new Runnable() {

				public void run() {
					connector.finishConnect(null, e);
				}
			});
		} catch (final Exception e) {

			context.getThreadPool().dispatch(new Runnable() {

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
