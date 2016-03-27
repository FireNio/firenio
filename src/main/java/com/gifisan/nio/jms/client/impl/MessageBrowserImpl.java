package com.gifisan.nio.jms.client.impl;

import java.io.IOException;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.nio.client.ClientResponse;
import com.gifisan.nio.client.ClientSesssion;
import com.gifisan.nio.jms.JMSException;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.jms.client.MessageBrowser;
import com.gifisan.nio.jms.client.MessageDecoder;

public class MessageBrowserImpl extends JMSConnectonImpl implements MessageBrowser{

	public MessageBrowserImpl(ClientSesssion session) throws JMSException {
		super(session);
	}


	public Message browser(String messageID) throws JMSException {
		JSONObject param = new JSONObject();
		param.put("messageID", messageID);
		param.put("cmd", "browser");
		
		ClientResponse response;
		try {
			response = session.request("JMSBrowserServlet",param.toJSONString());
		} catch (IOException e) {
			throw new JMSException(e.getMessage(),e);
		}
		
		
		return MessageDecoder.decode(response);
	}


	public int size() throws JMSException {
		String param = "{cmd:\"size\"}";
		
		ClientResponse response;
		try {
			response = session.request("JMSBrowserServlet",param);
		} catch (IOException e) {
			throw new JMSException(e.getMessage(),e);
		}
		return Integer.parseInt(response.getText());
	}



}
