package com.yoocent.mtp.jms;

import com.yoocent.mtp.common.StringUtil;
import com.yoocent.mtp.server.Request;

public class JMSMessage {

	public static JMSMessage newMessage(Request request) throws JMSException {
		
		String serviceName = request.getStringParameter("service-name");
		
		if (StringUtil.isBlankOrNull(serviceName)) {
			throw new JMSException("null service name");
		}
		
		return new JMSMessage(request, serviceName);
		
	}
	
	private Request request = null;
	
	private String serviceName = null;
	
	private String content = null;


	private JMSMessage(Request request,String serviceName) {
		this.request = request;
		this.serviceName = serviceName;
		
	}


	public String getContent() {
		return content;
	}
	
	
	public Request getRequest() {
		return request;
	}
	
	public String getServiceName() {
		return serviceName;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
