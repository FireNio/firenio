package com.gifisan.nio.connector;

import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import com.gifisan.nio.component.DefaultTCPEndPoint;
import com.gifisan.nio.component.EndPointWriter;
import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.component.SelectionAcceptor;
import com.gifisan.nio.component.TCPEndPoint;

public class TCPSelectionConnector implements SelectionAcceptor {

	private EndPointWriter	endPointWriter;
	private Selector		selector;
	private TCPConnector	connector;
	private NIOContext		context;

	public TCPSelectionConnector(NIOContext context, TCPConnector connector, EndPointWriter endPointWriter) {
		this.endPointWriter = endPointWriter;
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

	private TCPEndPoint attachEndPoint(NIOContext context, EndPointWriter endPointWriter, SelectionKey selectionKey)
			throws SocketException {

		TCPEndPoint endPoint = (TCPEndPoint) selectionKey.attachment();

		if (endPoint == null) {

			endPoint = new DefaultTCPEndPoint(context, selectionKey, endPointWriter);

			selectionKey.attach(endPoint);
		}

		return endPoint;
	}

	private void finishConnect(SelectionKey selectionKey, SocketChannel channel) {

		try {

			channel.finishConnect();

			channel.register(selector, SelectionKey.OP_READ);

			final TCPEndPoint endPoint = attachEndPoint(context, endPointWriter, selectionKey);

			context.getThreadPool().dispatch(new Runnable() {

				public void run() {
					connector.finishConnect(endPoint, null);
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
