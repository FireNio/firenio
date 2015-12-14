package com.gifisan.mtp.server.session;

import com.gifisan.mtp.component.AttributesImpl;
import com.gifisan.mtp.server.InnerEndPoint;
import com.gifisan.mtp.server.context.ServletContext;

public class MTPSession extends AttributesImpl implements Session{

	private long creationTime = System.currentTimeMillis();
	
	private ServletContext context = null;

	private String id = null;
	
	private InnerEndPoint endPoint = null;

	private long lastuse = System.currentTimeMillis();

	private long maxInactiveInterval = 30 * 60 * 1000;

	public MTPSession(ServletContext context,InnerEndPoint endPoint,String sessionID) {
		this.context = context;
		this.endPoint = endPoint;
		this.id = sessionID;
	}

	public void active(InnerEndPoint endPoint) {
		this.endPoint = endPoint;
		this.lastuse = System.currentTimeMillis();
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
		return !endPoint.isEndConnect() || System.currentTimeMillis() - lastuse < maxInactiveInterval  ;
	}

	public void setMaxInactiveInterval(long millisecond) {
		this.maxInactiveInterval = millisecond;
	}

	public boolean connecting() {
		return !endPoint.isEndConnect();
	}

	
	public int getComment() {
		return endPoint.comment();
	}

	
	public void setComment(int comment) {
		endPoint.setComment(comment);
		
	}

	
	
	public Object attachment() {
		return this.endPoint.attachment();
	}

	
	public void attach(Object attachment) {
		
		this.endPoint.attach(attachment);
	}
	
}
