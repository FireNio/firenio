package com.gifisan.nio.server.session;

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
	
	public void onDestroy(Session session) {
		this._listener.onDestroy(session);
		
	}
	
	
	
}
