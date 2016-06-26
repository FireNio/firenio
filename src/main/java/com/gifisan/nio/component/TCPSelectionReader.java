package com.gifisan.nio.component;

import java.net.SocketException;
import java.nio.channels.SelectionKey;

import com.gifisan.nio.acceptor.ServerTCPEndPoint;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.future.IOReadFuture;
import com.gifisan.nio.component.protocol.ProtocolDecoder;

public class TCPSelectionReader implements SelectionAcceptor {

	private ReadFutureAcceptor	readFutureAcceptor	;
	private NIOContext			context			;
	private EndPointWriter		endPointWriter		;

	public TCPSelectionReader(NIOContext context,EndPointWriter endPointWriter) {
		this.context = context;
		this.endPointWriter = endPointWriter;
		this.readFutureAcceptor = context.getReadFutureAcceptor();
	}
	
	private TCPEndPoint getEndPoint(SelectionKey selectionKey) throws SocketException{
		
		TCPEndPoint endPoint = (TCPEndPoint) selectionKey.attachment();
		
		if (endPoint == null) {
			endPoint = new ServerTCPEndPoint(context, selectionKey,endPointWriter);
			selectionKey.attach(endPoint);
		}
		return endPoint;
		
	}

	public void accept(SelectionKey selectionKey) throws Exception {

		NIOContext context = this.context;

		TCPEndPoint endPoint = getEndPoint(selectionKey);

		if (endPoint.isEndConnect()) {
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

			readFutureAcceptor.accept(future.getSession(), future);
		}
	}

}
