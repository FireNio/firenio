package com.gifisan.mtp.component;

import java.io.IOException;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.mtp.server.Attributes;
import com.gifisan.mtp.server.ServerEndPoint;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.ServletContext;
import com.gifisan.mtp.server.session.MTPSessionFactory;
import com.gifisan.mtp.server.session.Session;

public class MTPServletRequest extends AttributesImpl implements Attributes, Request {

	private boolean			closeCommand	= false;
	private ServerEndPoint		endPoint		= null;
	private JSONObject		parameters		= null;
	private ProtocolDecoder		protocolDecoder	= null;
	private String			serviceName		= null;
	private Session			session		= null;
	private ExecutorThreadPool	threadPool		= null;

	public MTPServletRequest(ServletContext context, ServerEndPoint endPoint,ExecutorThreadPool threadPool) throws IOException {
		this.protocolDecoder = endPoint.getProtocolDecoder();
		this.closeCommand = endPoint.isEndConnect();
		if (closeCommand) {
			return;
		}
		this.endPoint = endPoint;
		this.serviceName = protocolDecoder.getServiceName();
		this.parameters = protocolDecoder.getParameters();

		String sessionID = protocolDecoder.getSessionID();
		if (sessionID != null) {
			MTPSessionFactory factory = context.getMTPSessionFactory();
			this.session = factory.getSession(context, endPoint, sessionID);

		}
		this.threadPool = threadPool;
	}
	
	public ExecutorThreadPool getExecutorThreadPool(){
		return this.threadPool;
	}

	public boolean getBooleanParameter(String key) {
		return parameters.getBooleanValue(key);

	}

	public MTPRequestInputStream getInputStream() {

		return protocolDecoder.getInputStream();
	}

	public int getIntegerParameter(String key) {
		if (parameters == null) {
			return 0;
		}
		return parameters.getIntValue(key);

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

	public long getLongParameter(String key) {
		if (parameters == null) {
			return 0;
		}
		return parameters.getLongValue(key);

	}

	public int getMaxIdleTime() {
		return endPoint.getMaxIdleTime();
	}

	public Object getObjectParameter(String key) {
		if (parameters == null) {
			return null;
		}
		return parameters.get(key);

	}

	public String getParameter(String key) {
		if (parameters == null) {
			return null;
		}
		return parameters.getString(key);

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

	public boolean isCloseCommand() {
		return closeCommand;
	}

	public boolean isOpened() {
		return endPoint.isOpened();
	}

	public void setCloseCommand(boolean close) {
		this.closeCommand = close;

	}

}
