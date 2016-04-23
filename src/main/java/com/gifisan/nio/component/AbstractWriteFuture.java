package com.gifisan.nio.component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import com.gifisan.nio.client.IOWriteFuture;
import com.gifisan.nio.server.session.Session;
import com.gifisan.nio.service.FutureImpl;

public abstract class AbstractWriteFuture extends FutureImpl implements IOWriteFuture {

	private IOExceptionHandle	handle		= null;
	private Session			session		= null;
	protected EndPoint			endPoint		= null;
	protected ByteBuffer		textBuffer	= null;
	protected InputStream		inputStream	= null;
	private byte[]			textCache		= null;

	public AbstractWriteFuture(IOExceptionHandle handle,String serviceName, ByteBuffer textBuffer, byte[] textCache, Session session) {
		this.handle = handle;
		this.endPoint = ((AbstractSession)session).getEndPoint();
		this.session = session;
		this.textBuffer = textBuffer;
		this.textCache = textCache;
		this.serviceName = serviceName;
	}

	protected void attackNetwork(int length) {

		endPoint.attackNetwork(length);
	}

	public void catchException(IOException e) {
		if (this.handle == null) {
			return;
		}
		this.handle.handle(session, this, e);
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

	public InputStream getInputStream() {
		return inputStream;
	}

	public String getText() {
		if (text == null) {
			text = new String(textCache, session.getContext().getEncoding());
		}
		return text;
	}

}
