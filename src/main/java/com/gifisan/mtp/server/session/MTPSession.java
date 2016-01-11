package com.gifisan.mtp.server.session;

import com.gifisan.mtp.component.AttributesImpl;
import com.gifisan.mtp.component.ExecutorThreadPool;
import com.gifisan.mtp.component.MTPServletRequest;
import com.gifisan.mtp.component.MTPServletResponse;
import com.gifisan.mtp.schedule.ServletAcceptJob;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;
import com.gifisan.mtp.server.ServerEndPoint;
import com.gifisan.mtp.server.ServletContext;

public class MTPSession extends AttributesImpl implements InnerSession {

	private Object				attachment		= null;
	private ServletContext		context			= null;
	private long				creationTime		= System.currentTimeMillis();
	private ServerEndPoint		endPoint			= null;
	private MTPSessionFactory	factory			= null;
	private String				sessionID			= null;
	private long				lastuse			= creationTime;
	private SessionEventListener	listener			= null;
	private long				maxInactiveInterval	= 10 * 60 * 1000;
	private MTPServletRequest	request			= null;
	private MTPServletResponse	response			= null;
	private ServletAcceptJob		acceptJob			= null;
	private boolean			active			= true;

	public MTPSession(ServletContext context, ServerEndPoint endPoint, String sessionID, MTPSessionFactory factory,
			ExecutorThreadPool threadPool, ServletAcceptJob acceptJob) {
		this.sessionID = sessionID;
		this.context = context;
		this.endPoint = endPoint;
		this.factory = factory;
		this.acceptJob = acceptJob;
		this.request = new MTPServletRequest(threadPool, this);
		this.response = new MTPServletResponse();
//		this.maxInactiveInterval	= 10;

	}
	
	public boolean active(ServerEndPoint endPoint) {
		this.endPoint = endPoint;
		this.lastuse = System.currentTimeMillis();
		boolean _active = active;
		active = true;
		return _active;
	}

	public void attach(Object attachment) {
		this.attachment = attachment;
	}

	public Object attachment() {
		return this.attachment;
	}

	public boolean connecting() {
		return !endPoint.isEndConnect();
	}

	public void destroy() {
		this.lastuse = 0;
		this.endPoint.endConnect();
		this.factory.remove(this);
	}

	public void destroyImmediately() {
		this.active = false;
		if (listener != null) {
			listener.onDestroy(this);
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

	public String getSessionID() {
		return this.sessionID;
	}

	public boolean isValid() {
		return connecting() || System.currentTimeMillis() - lastuse < maxInactiveInterval;
	}

	public void setComment(int comment) {
		endPoint.setComment(comment);

	}

	public void setEventListener(SessionEventListener listener) {
		this.listener = listener;

	}

	public void setMaxInactiveInterval(long millisecond) {
		this.maxInactiveInterval = millisecond;
	}

	public ServletAcceptJob updateServletAcceptJob(ServerEndPoint endPoint) {

		return acceptJob.update(endPoint, request.update(endPoint), response.update(endPoint));

	}

	public Request updateRequest(ServerEndPoint endPoint) {
		return request.update(endPoint);
	}

	public Response updateResponse(ServerEndPoint endPoint) {
		return response.update(endPoint);
	}

}
