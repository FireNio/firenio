package com.gifisan.nio.component;

import com.gifisan.nio.concurrent.ExecutorThreadPool;
import com.gifisan.nio.server.InnerRequest;
import com.gifisan.nio.server.ServerEndPoint;
import com.gifisan.nio.server.session.Session;

public class NIOServletRequest implements InnerRequest {

	private ServerEndPoint		endPoint		= null;
	private RequestParam		parameters	= null;
	private ServerProtocolData	data			= null;
	private String				serviceName	= null;
	private Session			session		= null;
	private ExecutorThreadPool	threadPool	= null;
	private String				content		= null;

	public NIOServletRequest(ExecutorThreadPool threadPool, Session session) {
		this.threadPool = threadPool;
		this.session = session;
	}

	public ExecutorThreadPool getExecutorThreadPool() {
		return this.threadPool;
	}

	public InputStream getInputStream() {

		return data.getInputStream();
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

	public Session getSession() {

		return session;
	}

	public boolean isBlocking() {
		return endPoint.isBlocking();
	}

	public boolean isOpened() {
		return endPoint.isOpened();
	}

	public RequestParam getParameters() {
		if (parameters == null) {
			parameters = new DefaultParameters(content);
		}
		return parameters;
	}

	public void update(ServerEndPoint endPoint, ServerProtocolData data) {
		this.endPoint = endPoint;
		this.data = data;
		this.content = data.getText();
		this.serviceName = data.getServiceName();
		this.parameters = null;
	}

	public String getContent() {
		return this.content;
	}

}
