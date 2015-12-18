package com.gifisan.mtp.component;

import java.io.IOException;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.mtp.server.Attributes;
import com.gifisan.mtp.server.InnerEndPoint;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.context.ServletContext;
import com.gifisan.mtp.server.session.MTPSessionFactory;
import com.gifisan.mtp.server.session.Session;

public class MTPServletRequest extends AttributesImpl implements Attributes, Request{

	private boolean closeCommand 	= false;
	private InnerEndPoint endPoint = null;
	private JSONObject parameters 	= null;
	private MTPParser parser 		= null;
	private String serviceName 		= null;
	private Session session 		= null;
	
	public MTPServletRequest(ServletContext context,InnerEndPoint endPoint) throws IOException {
		this.parser = endPoint.getParser();
		this.closeCommand = endPoint.isEndConnect();
		if (closeCommand) {
			return;
		}
		this.endPoint = endPoint;
		this.serviceName = parser.getServiceName();
		this.parameters = parser.getParameters();
		
		String sessionID = parser.getSessionID();
		if (sessionID != null) {
			MTPSessionFactory factory = context.getMTPSessionFactory();
			this.session = factory.getSession(context,endPoint,sessionID);
			
		}
	}
	
	public boolean getBooleanParameter(String key){
		return parameters.getBooleanValue(key);
		
	}

	public MTPRequestInputStream getInputStream(){
		
		return parser.getInputStream();
	}
	
	public int getIntegerParameter(String key){
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

	public long getLongParameter(String key){
		if (parameters == null) {
			return 0;
		}
		return parameters.getLongValue(key);
		
	}

	public int getMaxIdleTime() {
		return endPoint.getMaxIdleTime();
	}

	public Object getObjectParameter(String key){
		if (parameters == null) {
			return null;
		}
		return parameters.get(key);
		
	}

	public String getParameter(String key){
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
	
	public Session getSession(){
		
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
