package com.gifisan.nio.component;

import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.SelectionKey;
import java.util.List;

import com.gifisan.nio.client.IOWriteFuture;
import com.gifisan.nio.server.NIOContext;

public class NIOSelectionWriter implements SelectionAcceptor {

	private NIOContext	context = null;
	
	public NIOSelectionWriter(NIOContext context) {
		this.context = context;
	}
	
	private EndPoint getEndPoint(SelectionKey selectionKey) throws SocketException{
		
		EndPoint endPoint = (EndPoint) selectionKey.attachment();
		
		if (endPoint == null) {
			endPoint = new NIOEndPoint(context, selectionKey);
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

		List<IOWriteFuture> writers = endPoint.getWriter();

		EndPointWriter endPointWriter = context.getEndPointWriter();

		for (IOWriteFuture writer : writers) {
			endPointWriter.offer(writer);
		}

		writers.clear();

		selectionKey.interestOps(SelectionKey.OP_READ);
	}


}
