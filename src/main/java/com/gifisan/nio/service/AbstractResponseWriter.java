package com.gifisan.nio.service;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.CatchWriteException;
import com.gifisan.nio.component.EndPoint;
import com.gifisan.nio.server.InnerRequest;

public abstract class AbstractResponseWriter implements ResponseWriter {

	protected ByteBuffer		buffer			= null;
	protected EndPoint			endPoint			= null;
	private byte				sessionID			= 0;
	private InnerRequest		request			= null;
	private CatchWriteException	catchWriteException	= null;

	public AbstractResponseWriter(ByteBuffer buffer, EndPoint endPoint, byte sessionID, InnerRequest request,
			CatchWriteException catchWriteException) {
		this.buffer = buffer;
		this.endPoint = endPoint;
		this.sessionID = sessionID;
		this.request = request;
		this.catchWriteException = catchWriteException;
	}

	public EndPoint getEndPoint() {
		return endPoint;
	}

	public byte getSessionID() {
		return sessionID;
	}

	public InnerRequest getRequest() {
		return request;
	}

	public void catchException(Request request, IOException e) {
		if (this.catchWriteException == null) {
			return;
		}
		this.catchWriteException.catchException(request, e);
	}

}
