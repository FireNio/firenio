package com.gifisan.nio.component;

import java.nio.channels.SelectionKey;

import com.gifisan.nio.component.protocol.ProtocolDecoder;
import com.gifisan.nio.component.protocol.future.IOReadFuture;

public class TCPSelectionReader implements SelectionAcceptor {

	private ReadFutureAcceptor	readFutureAcceptor;

	public TCPSelectionReader(NIOContext context) {
		this.readFutureAcceptor = context.getReadFutureAcceptor();
	}

	public void accept(SelectionKey selectionKey) throws Exception {

		TCPEndPoint endPoint = (TCPEndPoint) selectionKey.attachment();

		if (!endPoint.isOpened()) {
			return;
		}

		IOReadFuture future = endPoint.getReadFuture();

		if (future == null) {
			
			ProtocolDecoder decoder = endPoint.getProtocolDecoder();

			future = decoder.decode(endPoint);

			if (future == null) {
				return;
			}

			endPoint.setReadFuture(future);
		}

		if (future.read()) {

			endPoint.setReadFuture(null);

			Session session = endPoint.getSession();

			session.active();

			readFutureAcceptor.accept(session, future);
		}
	}

}
