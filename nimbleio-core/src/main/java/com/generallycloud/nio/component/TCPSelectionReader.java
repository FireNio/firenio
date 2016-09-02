package com.generallycloud.nio.component;

import java.nio.channels.SelectionKey;

import com.generallycloud.nio.component.protocol.IOReadFuture;
import com.generallycloud.nio.component.protocol.ProtocolDecoder;

public class TCPSelectionReader implements SelectionAcceptor {
	
	private IOReadFutureAcceptor	ioReadFutureAcceptor;

	public TCPSelectionReader(NIOContext context) {
		this.ioReadFutureAcceptor = context.getIOReadFutureAcceptor();
	}

	public void accept(SelectionKey selectionKey) throws Exception {

		TCPEndPoint endPoint = (TCPEndPoint) selectionKey.attachment();

		if (endPoint == null || !endPoint.isOpened()) {
			//该EndPoint已经被关闭
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

		if (!future.read()) {

			return;
		}

		endPoint.setReadFuture(null);

		Session session = endPoint.getSession();
		
		session.active();
		
		ioReadFutureAcceptor.accept(session, future);
	}

}
