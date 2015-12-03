package com.yoocent.mtp.component;

import java.io.IOException;

import com.alibaba.fastjson.JSONObject;
import com.yoocent.mtp.server.Attributes;
import com.yoocent.mtp.server.InnerEndPoint;
import com.yoocent.mtp.server.InnerRequest;
import com.yoocent.mtp.server.context.ServletContext;
import com.yoocent.mtp.server.session.MTPSessionFactory;
import com.yoocent.mtp.server.session.Session;

public class MTPServletRequest extends AttributesImpl implements Attributes, InnerRequest{

	private InnerEndPoint endPoint = null;
	
	private JSONObject parameters = null;
	
	private MTPParser parser = null;
	
	private String serviceName = null;
	
	private Session session = null;
	
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

	public String getStringParameter(String key){
		if (parameters == null) {
			return null;
		}
		return parameters.getString(key);
		
	}
	
	public boolean isBlocking() {
		return endPoint.isBlocking();
	}
	
	public boolean isOpened() {
		return endPoint.isOpened();
	}
	
	private boolean closeCommand;

	public boolean isCloseCommand() {
		return closeCommand;
	}

	public void setCloseCommand(boolean close) {
		this.closeCommand = close;
		
	}
	
}
