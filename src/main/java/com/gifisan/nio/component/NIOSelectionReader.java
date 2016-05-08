package com.gifisan.nio.component;

import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.SelectionKey;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.future.IOReadFuture;
import com.gifisan.nio.component.protocol.ProtocolDecoder;
import com.gifisan.nio.server.NIOContext;

public class NIOSelectionReader implements SelectionAcceptor {

	private ReadFutureAcceptor	readFutureAcceptor	= null;
	private NIOContext			context			= null;
	private EndPointWriter		endPointWriter		= null;

	public NIOSelectionReader(NIOContext context,EndPointWriter endPointWriter) {
		this.context = context;
		this.endPointWriter = endPointWriter;
		this.readFutureAcceptor = context.getReadFutureAcceptor();
	}
	
	private EndPoint getEndPoint(SelectionKey selectionKey) throws SocketException{
		
		EndPoint endPoint = (EndPoint) selectionKey.attachment();
		
		if (endPoint == null) {
			endPoint = new NIOEndPoint(context, selectionKey,endPointWriter);
			selectionKey.attach(endPoint);
		}
		return endPoint;
		
	}

	public void accept(SelectionKey selectionKey) throws IOException {

		NIOContext context = this.context;

		EndPoint endPoint = getEndPoint(selectionKey);

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
