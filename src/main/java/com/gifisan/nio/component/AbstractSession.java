package com.gifisan.nio.component;

import java.net.SocketException;

import com.gifisan.nio.Attachment;
import com.gifisan.nio.server.session.Session;

public abstract class AbstractSession implements Session{

	private Attachment					attachment		= null;
	private long						creationTime		= System.currentTimeMillis();
	private EndPoint					endPoint			= null;
	private byte						sessionID			= 0;

	public AbstractSession(EndPoint endPoint, byte sessionID) {
		this.sessionID = sessionID;
		this.endPoint = endPoint;
	}

	public void attach(Attachment attachment) {
		this.attachment = attachment;
	}

	public Attachment attachment() {
		return this.attachment;
	}

	public void disconnect() {
		// FIXME ......
		this.endPoint.endConnect();
	}

	public long getCreationTime() {
		return this.creationTime;
	}

	public EndPoint getEndPoint() {
		return endPoint;
	}

	public String getLocalAddr() {
		return endPoint.getLocalAddr();
	}

	public String getLocalHost() {
		return endPoint.getLocalHost();
	}

	public int getLocalPort() {
		return endPoint.getLocalPort();
	}

	public int getMaxIdleTime() throws SocketException {
		return endPoint.getMaxIdleTime();
	}

	public String getRemoteAddr() {
		return endPoint.getRemoteAddr();
	}

	public String getRemoteHost() {
		return endPoint.getRemoteHost();
	}

	public int getRemotePort() {
		return endPoint.getRemotePort();
	}

	public boolean isBlocking() {
		return endPoint.isBlocking();
	}

	public boolean isOpened() {
		return endPoint.isOpened();
	}

	public byte getSessionID() {
		return sessionID;
	}
	
	
	
	
}
