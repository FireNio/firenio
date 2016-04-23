package com.gifisan.nio.component;

import java.net.SocketException;

import com.gifisan.nio.Attachment;
import com.gifisan.nio.server.session.Session;
import com.gifisan.nio.server.session.SessionEventListener;
import com.gifisan.nio.server.session.SessionEventListenerWrapper;
import com.gifisan.nio.service.ServiceAcceptor;

public abstract class AbstractSession extends AttributesImpl implements Session{

	private Attachment					attachment		= null;
	private long						creationTime		= System.currentTimeMillis();
	private byte						sessionID			= 0;
	protected EndPoint					endPoint			= null;
	private SessionEventListenerWrapper	lastListener		= null;
	private SessionEventListenerWrapper	listenerStub		= null;
	protected ServiceAcceptor serviceAcceptor = null;

	public AbstractSession(EndPoint endPoint, byte sessionID) {
		this.sessionID = sessionID;
		this.endPoint = endPoint;
	}

	public void addEventListener(SessionEventListener listener) {
		if (this.listenerStub == null) {
			this.listenerStub = new SessionEventListenerWrapper(listener);
			this.lastListener = this.listenerStub;
		} else {
			this.lastListener.setNext(new SessionEventListenerWrapper(listener));
		}
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

	protected EndPoint getEndPoint() {
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
	
	public void destroyImmediately() {

		SessionEventListenerWrapper listenerWrapper = this.listenerStub;

		for (; listenerWrapper != null;) {
			listenerWrapper.onDestroy(this);
			listenerWrapper = listenerWrapper.nextListener();
		}
	}

	public ServiceAcceptor getServiceAcceptor() {
		return serviceAcceptor;
	}
	
}
