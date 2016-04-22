package com.gifisan.nio.component;

import java.net.SocketException;
import java.nio.channels.SelectionKey;

import com.gifisan.nio.server.NIOContext;
import com.gifisan.nio.server.selector.SelectionAcceptor;

public abstract class AbstractNIOSelection implements SelectionAcceptor {

	protected NIOContext	context	= null;

	public AbstractNIOSelection(NIOContext context) {
		this.context = context;
	}

	protected EndPoint getEndPoint(NIOContext context, SelectionKey selectionKey) throws SocketException {

		Object attachment = selectionKey.attachment();

		if (isEndPoint(attachment)) {
			return (EndPoint) attachment;
		}

		EndPoint endPoint = new NIOEndPoint(selectionKey);

		selectionKey.attach(endPoint);

		return endPoint;

	}

	private boolean isEndPoint(Object object) {

		return object != null && (object.getClass() == NIOEndPoint.class || object instanceof EndPoint);

	}
}
