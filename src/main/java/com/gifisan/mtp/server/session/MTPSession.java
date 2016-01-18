package com.gifisan.mtp.server.session;

import com.gifisan.mtp.component.AttributesImpl;
import com.gifisan.mtp.component.MTPServletRequest;
import com.gifisan.mtp.component.MTPServletResponse;
import com.gifisan.mtp.component.ServletAcceptJobImpl;
import com.gifisan.mtp.component.ServletService;
import com.gifisan.mtp.concurrent.ExecutorThreadPool;
import com.gifisan.mtp.schedule.ServletAcceptJob;
import com.gifisan.mtp.server.ServerEndPoint;
import com.gifisan.mtp.server.ServletContext;

public class MTPSession extends AttributesImpl implements InnerSession {

	private Object						attachment		= null;
	private ServletContext				context			= null;
	private long						creationTime		= System.currentTimeMillis();
	private ServerEndPoint				endPoint			= null;
	private MTPSessionFactory			factory			= null;
	private long						sessionID			= 0;
	private long						lastuse			= creationTime;
	private SessionEventListenerWrapper	listenerStub		= null;
	private SessionEventListenerWrapper	lastListener		= null;
	private long						maxInactiveInterval	= 10 * 60 * 1000;
	private MTPServletRequest			request			= null;
	private MTPServletResponse			response			= null;
	private ServletAcceptJob				acceptJob			= null;

	public MTPSession(ServletContext context, ServerEndPoint endPoint, MTPSessionFactory factory,
			ExecutorThreadPool threadPool, ServletService service, long sessionID) {
		this.sessionID = sessionID;
		this.context = context;
		this.endPoint = endPoint;
		this.factory = factory;
		this.request = new MTPServletRequest(threadPool, this);
		this.response = new MTPServletResponse();
		this.acceptJob = new ServletAcceptJobImpl(endPoint, service, request, response);
	}

	public void attach(Object attachment) {
		this.attachment = attachment;
	}

	public Object attachment() {
		return this.attachment;
	}

	public void destroy() {
		this.endPoint.endConnect();
	}

	public void destroyImmediately() {
		factory.remove(this);
		SessionEventListenerWrapper listenerWrapper = this.listenerStub;

		for (;listenerWrapper != null;) {
			listenerWrapper.onDestroy(this);
			listenerWrapper = listenerWrapper.nextListener();
		}

	}

	public int getComment() {
		return endPoint.comment();
	}

	public long getCreationTime() {
		return this.creationTime;
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

	public long getSessionID() {
		return this.sessionID;
	}

	public boolean isValid() {
		// TODO yanzheng
		return true;
	}

	public void setComment(int comment) {
		endPoint.setComment(comment);

	}

	public void addEventListener(SessionEventListener listener) {
		if (this.listenerStub == null) {
			this.listenerStub = new SessionEventListenerWrapper(listener);
			this.lastListener = this.listenerStub;
		} else {
			this.lastListener.setNext(new SessionEventListenerWrapper(listener));
		}

	}

	public void setMaxInactiveInterval(long millisecond) {
		this.maxInactiveInterval = millisecond;
	}

	public ServletAcceptJob updateServletAcceptJob() {
		this.lastuse = System.currentTimeMillis();
		return acceptJob.update(endPoint, request.update(endPoint), response.update(endPoint));
	}

}
