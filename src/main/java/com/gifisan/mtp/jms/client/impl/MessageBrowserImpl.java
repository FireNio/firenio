package com.gifisan.mtp.jms.client.impl;

import java.io.IOException;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.mtp.client.Response;
import com.gifisan.mtp.jms.JMSException;
import com.gifisan.mtp.jms.Message;
import com.gifisan.mtp.jms.MessageBrowser;
import com.gifisan.mtp.jms.client.MessageDecoder;
import com.gifisan.mtp.jms.server.JMSBrowserServlet;

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
		
		
		return MessageDecoder.decode(response);
	}


	public int size() throws JMSException {
		// TODO Auto-generated method stub
		return 0;
	}



}
