package com.gifisan.nio.component;

import java.nio.channels.SelectionKey;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.protocol.ProtocolDecoder;
import com.gifisan.nio.component.protocol.future.IOReadFuture;

public class TCPSelectionReader implements SelectionAcceptor {

	private ReadFutureAcceptor	readFutureAcceptor	;
	private NIOContext			context			;

	public TCPSelectionReader(NIOContext context) {
		this.context = context;
		this.readFutureAcceptor = context.getReadFutureAcceptor();
	}
	
	public void accept(SelectionKey selectionKey) throws Exception {

		NIOContext context = this.context;

		TCPEndPoint endPoint = (TCPEndPoint) selectionKey.attachment();

		if (endPoint.isEndConnect()) {
			if (endPoint.isOpened()) {
				CloseUtil.close(endPoint);
			}
			return;
		}

		IOReadFuture future = endPoint.getReadFuture();

		if (future == null) {

			ProtocolDecoder decoder = context.getProtocolDecoder();

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
