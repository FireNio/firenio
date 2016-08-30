package com.generallycloud.nio.component;

public class SessionEventListenerWrapper implements SessionEventListener{
	
	private SessionEventListener _listener = null;
	
	private SessionEventListenerWrapper next = null;
	
	public SessionEventListenerWrapper(SessionEventListener _listener) {
		this._listener = _listener;
	}

	public SessionEventListenerWrapper nextListener(){
		return this.next;
	}
	
	public void setNext(SessionEventListenerWrapper listener){
		this.next = listener;
	}

	public void sessionOpened(Session session) {
		this._listener.sessionOpened(session);
	}

	public void sessionClosed(Session session) {
		this._listener.sessionClosed(session);
	}

	public void sessionIdled(Session session, long lastIdleTime,long currentTime) {
		this._listener.sessionIdled(session, lastIdleTime,currentTime);
	}
}
