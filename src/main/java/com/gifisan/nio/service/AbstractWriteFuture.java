package com.gifisan.nio.service;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.IOExceptionHandle;
import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.server.session.Session;

public abstract class AbstractWriteFuture implements WriteFuture {

	private IOExceptionHandle	handle	= null;
	private Session			session			= null;
	protected EndPoint			endPoint			= null;
	protected ByteBuffer		textBuffer		= null;

	public AbstractWriteFuture(IOExceptionHandle handle, ByteBuffer textBuffer, Session session) {
		this.handle = handle;
		this.endPoint = session.getEndPoint();
		this.session = session;
		this.textBuffer = textBuffer;
	}

	protected void attackNetwork(int length) {

		endPoint.attackNetwork(length);
	}

	public void catchException(IOException e) {
		if (this.handle == null) {
			return;
		}
		this.handle.handle(session, e);
	}

	public EndPoint getEndPoint() {
		return endPoint;
	}

	public boolean isNetworkWeak() {
		return endPoint.isNetworkWeak();
	}

	public Session getSession() {
		return session;
	}

}
