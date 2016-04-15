package com.gifisan.nio.server.session;

import java.io.OutputStream;

import com.gifisan.nio.Attachment;
import com.gifisan.nio.component.AttributesImpl;
import com.gifisan.nio.component.NormalServiceAcceptor;
import com.gifisan.nio.component.ProtocolData;
import com.gifisan.nio.concurrent.ExecutorThreadPool;
import com.gifisan.nio.server.InnerRequest;
import com.gifisan.nio.server.InnerResponse;
import com.gifisan.nio.server.ServerContext;
import com.gifisan.nio.server.ServerEndPoint;
import com.gifisan.nio.server.selector.ServiceAcceptorJob;
import com.gifisan.nio.service.ServiceRequest;
import com.gifisan.nio.service.ServiceResponse;

public class NIOSession extends AttributesImpl implements InnerSession {

	private Attachment					attachment		= null;
	private ServerContext				context			= null;
	private long						creationTime		= System.currentTimeMillis();
	private ServerEndPoint				endPoint			= null;
	private byte						sessionID			= 0;
	private SessionEventListenerWrapper	listenerStub		= null;
	private SessionEventListenerWrapper	lastListener		= null;
	private ServiceRequest				request			= null;
	private ServiceResponse				response			= null;
	private ServiceAcceptorJob			acceptor			= null;
	private OutputStream				serverOutputStream	= null;
	private ExecutorThreadPool			executorThreadPool	= null;
	private boolean					stream			= false;

	public NIOSession(ServerEndPoint endPoint, byte sessionID) {
		this.sessionID = sessionID;
		this.context = endPoint.getContext();
		this.endPoint = endPoint;
		this.request = new ServiceRequest(this);
		this.executorThreadPool = context.getExecutorThreadPool();
		this.acceptor = new NormalServiceAcceptor(endPoint, context.getFilterService(), request, response);
	}

	public void attach(Attachment attachment) {
		this.attachment = attachment;
	}

	public Attachment attachment() {
		return this.attachment;
	}

	public void disconnect() {
		this.endPoint.endConnect();
	}

	public void destroyImmediately() {

		SessionEventListenerWrapper listenerWrapper = this.listenerStub;

		for (; listenerWrapper != null;) {
			listenerWrapper.onDestroy(this);
			listenerWrapper = listenerWrapper.nextListener();
		}

	}

	public long getCreationTime() {
		return this.creationTime;
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

	public ServiceAcceptorJob updateAcceptor(ProtocolData protocolData) {
		return acceptor.update(endPoint, protocolData);
	}

	public ServiceAcceptorJob updateAcceptor() {
		return acceptor;
	}

	public int getEndpointMark() {
		return endPoint.getMark();
	}

	public void setEndpointMark(int mark) {
		endPoint.setMark(mark);
	}

	public InnerRequest getRequest() {
		return request;
	}

	public InnerResponse getResponse() {
		return response;
	}

	public OutputStream getServerOutputStream() {
		return serverOutputStream;
	}

	public void setServerOutputStream(OutputStream serverOutputStream) {
		this.serverOutputStream = serverOutputStream;
	}

	public ExecutorThreadPool getExecutorThreadPool() {
		return this.executorThreadPool;
	}

	public boolean isStream() {
		return stream;
	}

	public void setStream(boolean stream) {
		this.stream = stream;
	}

}
