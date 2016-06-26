package com.gifisan.nio.component;

import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.SelectionKey;

import com.gifisan.nio.acceptor.ServerTCPEndPoint;

public class TCPSelectionWriter implements SelectionAcceptor {

	private NIOContext		context		;
	private EndPointWriter	endPointWriter	;

	public TCPSelectionWriter(NIOContext context, EndPointWriter endPointWriter) {
		this.context = context;
		this.endPointWriter = endPointWriter;
	}

	private TCPEndPoint getEndPoint(SelectionKey selectionKey) throws SocketException {

		TCPEndPoint endPoint = (TCPEndPoint) selectionKey.attachment();

		if (endPoint == null) {
			// maybe not happen
			endPoint = new ServerTCPEndPoint(context, selectionKey, endPointWriter);
			selectionKey.attach(endPoint);
		}
		return endPoint;

	}

	public void accept(SelectionKey selectionKey) throws IOException {

		TCPEndPoint endPoint = getEndPoint(selectionKey);

		if (endPoint.isEndConnect()) {
			return;
		}

		endPoint.flushWriters();

	}

}
