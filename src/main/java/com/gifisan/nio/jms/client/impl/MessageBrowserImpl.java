package com.gifisan.nio.jms.client.impl;

import java.io.IOException;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.nio.client.ClientSesssion;
import com.gifisan.nio.client.Response;
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
		
		Response response;
		try {
			response = session.request("JMSBrowserServlet",param.toJSONString());
		} catch (IOException e) {
			throw new JMSException(e.getMessage(),e);
		}
		
		
		return MessageDecoder.decode(response);
	}


	public int size() throws JMSException {
		// TODO jisuan
		return 0;
	}



}
