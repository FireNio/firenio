package com.yoocent.mtp.server.session;

import com.yoocent.mtp.component.AttributesImpl;
import com.yoocent.mtp.server.context.ServletContext;

public class MTPSession extends AttributesImpl implements Session{

	private long creationTime = System.currentTimeMillis();
	
	private ServletContext context = null;

	private String id = null;

	private long lastuse = System.currentTimeMillis();

	private long maxInactiveInterval = 30 * 60 * 1000;

	public MTPSession(ServletContext context,String sessionID) {
		this.context = context;
		this.id = sessionID;
	}

	public void active() {
		lastuse = System.currentTimeMillis();
	}

	public long getCreationTime() {
		return this.creationTime;
	}

	public String getSessionID() {
		return this.id;
	}

	public long getLastAccessedTime() {
		return this.lastuse;
	}

	public long getMaxInactiveInterval() {
		return this.maxInactiveInterval;
	}

	public ServletContext getServletContext() {
		return context;
	}

	public boolean isValid() {
		return System.currentTimeMillis() - lastuse < maxInactiveInterval;
	}

	public void setMaxInactiveInterval(long millisecond) {
		this.maxInactiveInterval = millisecond;
	}

	
}
