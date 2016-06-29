package com.gifisan.nio.connector;

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

	public TCPSelectionConnector(NIOContext context, Selector selector, TCPConnector connector,
			EndPointWriter endPointWriter) {
		this.endPointWriter = endPointWriter;
		this.selector = selector;
		this.connector = connector;
		this.context = context;
	}

	public void accept(SelectionKey selectionKey) throws Exception {

		SocketChannel channel = (SocketChannel) selectionKey.channel();

		// does it need connection pending ?
		if (channel.isConnectionPending()) {

			channel.finishConnect();

			//sk == selectionkey
			SelectionKey sk = channel.register(selector, SelectionKey.OP_READ);
			
			final TCPEndPoint endPoint = new DefaultTCPEndPoint(context, selectionKey, endPointWriter);

			sk.attach(endPoint);

			context.getThreadPool().dispatch(new Runnable() {
				
				public void run() {
					connector.finishConnect(endPoint);
				}
			});
		}
	}
}
