package com.gifisan.nio.jms.client.impl;

import java.io.IOException;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.jms.JMSException;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.jms.client.MessageBrowser;
import com.gifisan.nio.jms.client.MessageDecoder;
import com.gifisan.nio.jms.server.JMSBrowserServlet;

public class MessageBrowserImpl extends JMSConnectonImpl implements MessageBrowser{

	public MessageBrowserImpl(ClientSession session) throws JMSException {
		super(session);
	}


	public Message browser(String messageID) throws JMSException {
		JSONObject param = new JSONObject();
		param.put("messageID", messageID);
		param.put("cmd", JMSBrowserServlet.BROWSER);
		
		ReadFuture future;
		try {
			future = session.request("JMSBrowserServlet",param.toJSONString());
		} catch (IOException e) {
			throw new JMSException(e.getMessage(),e);
		}
		
		return MessageDecoder.decode(future);
	}


	public int size() throws JMSException {
		String param = "{cmd:\"0\"}";
		
		ReadFuture future;
		try {
			future = session.request("JMSBrowserServlet",param);
		} catch (IOException e) {
			throw new JMSException(e.getMessage(),e);
		}
		return Integer.parseInt(future.getText());
	}

	
	public boolean isOnline(String queueName) throws JMSException{
		
		JSONObject param = new JSONObject();
		param.put("queueName", queueName);
		param.put("cmd", JMSBrowserServlet.ONLINE);
		
		ReadFuture future;
		try {
			future = session.request("JMSBrowserServlet",param.toJSONString());
		} catch (IOException e) {
			throw new JMSException(e.getMessage(),e);
		}
		
		return "T".equals(future.getText());
	}


}
