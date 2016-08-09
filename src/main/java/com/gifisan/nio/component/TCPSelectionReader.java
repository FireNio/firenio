package com.gifisan.nio.component;

import java.nio.channels.SelectionKey;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.protocol.ProtocolDecoder;
import com.gifisan.nio.component.protocol.future.IOReadFuture;

public class TCPSelectionReader implements SelectionAcceptor {

	private ReadFutureAcceptor	readFutureAcceptor	;
	private ProtocolDecoder 		decoder			;

	public TCPSelectionReader(NIOContext context) {
		this.readFutureAcceptor = context.getReadFutureAcceptor();
		this.decoder = context.getProtocolDecoder();
	}
	
	public void accept(SelectionKey selectionKey) throws Exception {

		TCPEndPoint endPoint = (TCPEndPoint) selectionKey.attachment();

		if (endPoint.isEndConnect()) {
			if (endPoint.isOpened()) {
				CloseUtil.close(endPoint);
			}
			return;
		}

		IOReadFuture future = endPoint.getReadFuture();

		if (future == null) {

			future = decoder.decode(endPoint);

			if (future == null) {
				if (endPoint.isEndConnect()) {
					CloseUtil.close(endPoint);
				}
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
