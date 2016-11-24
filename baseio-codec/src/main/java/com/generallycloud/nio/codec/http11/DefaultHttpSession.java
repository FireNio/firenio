package com.generallycloud.nio.codec.http11;

import java.io.IOException;

import com.generallycloud.nio.AttributesImpl;
import com.generallycloud.nio.common.UUIDGenerator;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.ReadFuture;

public class DefaultHttpSession extends AttributesImpl implements HttpSession {

	private long			createTime	= System.currentTimeMillis();

	private SocketSession	ioSession;

	private long			lastAccessTime;

	private String			sessionID;

	private HttpContext		context;

	protected DefaultHttpSession(HttpContext context, SocketSession ioSession) {
		this.context = context;
		this.ioSession = ioSession;
		this.sessionID = UUIDGenerator.random().toString();
	}

	protected DefaultHttpSession(HttpContext context, SocketSession ioSession, String sessionID) {
		this.context = context;
		this.ioSession = ioSession;
		this.sessionID = sessionID;
	}

	public void active(SocketSession ioSession) {
		this.ioSession = ioSession;
		this.lastAccessTime = System.currentTimeMillis();
	}

	public void flush(ReadFuture future) throws IOException {
		ioSession.flush(future);
	}

	public long getCreateTime() {
		return createTime;
	}

	public SocketSession getIoSession() {
		return ioSession;
	}

	public long getLastAccessTime() {
		return lastAccessTime;
	}

	public String getSessionID() {
		return sessionID;
	}

	public boolean isValidate() {
		return System.currentTimeMillis() - lastAccessTime < 1000 * 60 * 30;
	}

	public HttpContext getContext() {
		return context;
	}
}
