package com.yoocent.mtp.jms.client.impl;

import java.io.IOException;

import com.alibaba.fastjson.JSONObject;
import com.yoocent.mtp.client.Response;
import com.yoocent.mtp.jms.JMSException;
import com.yoocent.mtp.jms.Message;
import com.yoocent.mtp.jms.MessageBrowser;
import com.yoocent.mtp.jms.client.MessageParser;
import com.yoocent.mtp.jms.server.JMSBrowserServlet;

public class MessageBrowserImpl extends ConnectonImpl implements MessageBrowser{


	public MessageBrowserImpl(String url, String sessionID) throws JMSException {
		super(url, sessionID);
	}
	

	public Message browser(String messageID) throws JMSException {
		JSONObject param = new JSONObject();
		param.put("messageID", messageID);
		
		Response response;
		try {
			response = client.request(JMSBrowserServlet.SERVICE_NAME,param.toJSONString() , 0);
		} catch (IOException e) {
			throw new JMSException(e.getMessage(),e);
		}
		
		
		return MessageParser.parse(response);
	}



}
