package com.gifisan.nio.service;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.IOReadFuture;
import com.gifisan.nio.server.session.Session;

public abstract class AbstractReadFuture extends ReadFutureImpl implements IOReadFuture {

	protected EndPoint			endPoint		= null;
	protected Session			session		= null;
	protected ByteBuffer		textBuffer	= null;

	public AbstractReadFuture(ByteBuffer textBuffer, Session session, String serviceName) {
		super(serviceName);
		this.endPoint = session.getEndPoint();
		this.session = session;
		this.textBuffer = textBuffer;
	}

	public EndPoint getEndPoint() {
		return endPoint;
	}

	public Session getSession() {
		return session;
	}

	public void catchException(IOException e) {
		if (this.handle == null) {
			return;
		}
		this.handle.handle(session, e);
	}


}
