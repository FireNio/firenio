package com.yoocent.mtp.jms.client.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.yoocent.mtp.client.Response;
import com.yoocent.mtp.jms.JMSException;
import com.yoocent.mtp.jms.Message;
import com.yoocent.mtp.jms.MessageConsumer;
import com.yoocent.mtp.jms.client.MessageParser;
import com.yoocent.mtp.jms.server.JMSConsumerServlet;

public class MessageConsumerImpl extends ConnectonImpl implements MessageConsumer{


	private String reviceParam = null;
	
	private String subscibeParam = null;


	private void initParam(String queueName,long timeout){
		Map<String, String> param = new HashMap<String, String>();
		param.put("queueName", queueName);
		param.put("timeout", String.valueOf(timeout));
		param.put("subscibe", "F");
		this.reviceParam = JSONObject.toJSONString(param);
		param.put("subscibe", "T");
		this.subscibeParam = JSONObject.toJSONString(param);
		
		
	}
	
	
	public MessageConsumerImpl(String url, String sessionID,String queueName,long timeout) throws JMSException {
		super(url, sessionID);
		this.initParam(queueName, timeout);
	}

	public void beginTransaction() throws JMSException {
		
		
		
	}

	public void commit() throws JMSException {
		
	}

	public void rollback() throws JMSException {
		
	}

	public Message revice() throws JMSException {
		Response response;
		try {
			response = client.request(JMSConsumerServlet.SERVICE_NAME,reviceParam , 0);
		} catch (IOException e) {
			throw new JMSException(e.getMessage(),e);
		}
		
		
		return MessageParser.parse(response);
	}

	
	public Message subscibe() throws JMSException {
		Response response;
		try {
			response = client.request(JMSConsumerServlet.SERVICE_NAME,subscibeParam , 0);
		} catch (IOException e) {
			throw new JMSException(e.getMessage(),e);
		}
		return MessageParser.parse(response);
	}
	
}
