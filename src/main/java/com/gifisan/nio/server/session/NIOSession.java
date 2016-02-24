package com.gifisan.nio.server.session;

import com.gifisan.nio.component.AttributesImpl;
import com.gifisan.nio.component.NIOServletRequest;
import com.gifisan.nio.component.NIOServletResponse;
import com.gifisan.nio.component.ServletAcceptJobImpl;
import com.gifisan.nio.schedule.ServletAcceptJob;
import com.gifisan.nio.server.ServerContext;
import com.gifisan.nio.server.ServerEndPoint;

public class NIOSession extends AttributesImpl implements InnerSession {

	private Object						attachment		= null;
	private ServerContext				context			= null;
	private long						creationTime		= System.currentTimeMillis();
	private ServerEndPoint				endPoint			= null;
	private byte						sessionID			= 0;
	private long						lastuse			= creationTime;
	private SessionEventListenerWrapper	listenerStub		= null;
	private SessionEventListenerWrapper	lastListener		= null;
	private NIOServletRequest			request			= null;
	private NIOServletResponse			response			= null;
	private ServletAcceptJob				acceptJob			= null;

	public NIOSession(ServerEndPoint endPoint, byte sessionID) {
		this.sessionID = sessionID;
		this.context = endPoint.getContext();
		this.endPoint = endPoint;
		this.request = new NIOServletRequest(context.getExecutorThreadPool(), this);
		this.response = new NIOServletResponse(endPoint,this);
		this.acceptJob = new ServletAcceptJobImpl(endPoint, context.getFilterService(), request, response);
	}

	public void attach(Object attachment) {
		this.attachment = attachment;
	}

	public Object attachment() {
		return this.attachment;
	}

	public void disconnect() {
		this.endPoint.endConnect();
	}

	public void destroyImmediately() {
		
		SessionEventListenerWrapper listenerWrapper = this.listenerStub;

		for (;listenerWrapper != null;) {
			listenerWrapper.onDestroy(this);
			listenerWrapper = listenerWrapper.nextListener();
		}

	}

	public long getCreationTime() {
		return this.creationTime;
	}

	public long getLastAccessedTime() {
		return this.lastuse;
	}

	public ServerContext getServerContext() {
		return context;
	}

	public byte getSessionID() {
		return this.sessionID;
	}

	public void addEventListener(SessionEventListener listener) {
		if (this.listenerStub == null) {
			this.listenerStub = new SessionEventListenerWrapper(listener);
			this.lastListener = this.listenerStub;
		} else {
			this.lastListener.setNext(new SessionEventListenerWrapper(listener));
		}
	}

	public ServletAcceptJob updateServletAcceptJob() {
		this.lastuse = System.currentTimeMillis();
		return acceptJob.update(endPoint);
	}

	public int getEndpointMark() {
		return endPoint.getMark();
	}

	public void setEndpointMark(int mark) {
		endPoint.setMark(mark);
	}
	
	

}
