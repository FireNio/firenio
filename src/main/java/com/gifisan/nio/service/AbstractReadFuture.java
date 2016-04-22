package com.gifisan.nio.service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.DefaultParameters;
import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.component.IOExceptionHandle;
import com.gifisan.nio.component.IOReadFuture;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.server.session.Session;

public abstract class AbstractReadFuture implements IOReadFuture {

	protected EndPoint			endPoint		= null;
	private String				serviceName	= null;
	protected Session			session		= null;
	protected ByteBuffer		textBuffer	= null;
	private String				text			= null;
	private Parameters			parameters	= null;
	protected OutputStream		outputStream	= null;
	protected IOExceptionHandle	handle		= null;

	public AbstractReadFuture(ByteBuffer textBuffer, Session session, String serviceName) {
		this.endPoint = session.getEndPoint();
		this.session = session;
		this.textBuffer = textBuffer;
		this.serviceName = serviceName;
	}

	public EndPoint getEndPoint() {
		return endPoint;
	}

	public String getServiceName() {
		return serviceName;
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

	public String getText() {
		if (text == null) {
			text = new String(textBuffer.array(), session.getContext().getEncoding());
		}
		return text;
	}

	public Parameters getParameters() {
		if (parameters == null) {
			parameters = new DefaultParameters(getText());
		}
		return parameters;
	}

	public void setIOEvent(OutputStream outputStream, IOExceptionHandle handle) {
		this.outputStream = outputStream;
		this.handle = handle;
	}

}
