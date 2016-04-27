package com.gifisan.nio.component;

import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.SelectionKey;

import com.gifisan.nio.server.NIOContext;

public class NIOSelectionWriter implements SelectionAcceptor {

	private NIOContext	context = null;
	
	public NIOSelectionWriter(NIOContext context) {
		this.context = context;
	}
	
	private EndPoint getEndPoint(SelectionKey selectionKey) throws SocketException{
		
		EndPoint endPoint = (EndPoint) selectionKey.attachment();
		
		if (endPoint == null) {
			// maybe not happen
			endPoint = new NIOEndPoint(context, selectionKey);
			selectionKey.attach(endPoint);
		}
		return endPoint;
		
	}

	public void accept(SelectionKey selectionKey) throws IOException {

		EndPoint endPoint = getEndPoint(selectionKey);

		if (endPoint.isEndConnect()) {
			return;
		}

		endPoint.flushWriters();
		
	}


}
