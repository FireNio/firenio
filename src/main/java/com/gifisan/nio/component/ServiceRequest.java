package com.gifisan.nio.component;

import com.gifisan.nio.server.InnerRequest;
import com.gifisan.nio.server.ServerEndPoint;
import com.gifisan.nio.server.session.NIOSession;

public class ServiceRequest implements InnerRequest {

	private ServerEndPoint		endPoint		= null;
	private Parameters			parameters	= null;
	private String				serviceName	= "";
	private NIOSession			session		= null;
	private String				content		= null;

	public ServiceRequest(NIOSession session) {
		this.session = session;
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

	public int getMaxIdleTime() {
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

	public String getServiceName() {
		return serviceName;
	}

	public NIOSession getSession() {

		return session;
	}

	public boolean isBlocking() {
		return endPoint.isBlocking();
	}

	public boolean isOpened() {
		return endPoint.isOpened();
	}

	public Parameters getParameters() {
		if (parameters == null) {
			parameters = new DefaultParameters(content);
		}
		return parameters;
	}

	public void update(ServerEndPoint endPoint, ProtocolData data) {
		this.endPoint = endPoint;
		this.content = data.getText();
		this.serviceName = data.getServiceName();
		this.parameters = null;
	}

	public String getContent() {
		return this.content;
	}

}
