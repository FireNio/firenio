package com.gifisan.mtp.jms.client.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.mtp.client.Response;
import com.gifisan.mtp.component.RESMessage;
import com.gifisan.mtp.component.RESMessageDecoder;
import com.gifisan.mtp.jms.JMSException;
import com.gifisan.mtp.jms.Message;
import com.gifisan.mtp.jms.MessageConsumer;
import com.gifisan.mtp.jms.client.MessageDecoder;
import com.gifisan.mtp.jms.server.JMSConsumerServlet;
import com.gifisan.mtp.jms.server.JMSTransactionServlet;

public class MessageConsumerImpl extends ConnectonImpl implements MessageConsumer {

	private String		reviceParam		= null;
	private String		subscibeParam		= null;
	private String		beginTransaction	= null;
	private String		commit			= null;
	private String		rollback			= null;

	private void initParam(String queueName, long timeout) {
		Map<String, String> param = new HashMap<String, String>();
		param.put("queueName", queueName);
		param.put("timeout", String.valueOf(timeout));
		param.put("subscibe", "F");
		this.reviceParam = JSONObject.toJSONString(param);
		param.put("subscibe", "T");
		this.subscibeParam = JSONObject.toJSONString(param);
	}

	public MessageConsumerImpl(String url, String sessionID, String queueName, long timeout) throws JMSException {
		super(url, sessionID);
		this.initParam(queueName, timeout);
	}

	private void transactioninit(){
		if (beginTransaction == null) {
			beginTransaction = "{action:\"begin\"}";
			commit = "{action:\"commit\"}";
			rollback = "{action:\"rollback\"}";
		}
	}
	
	public boolean beginTransaction() throws JMSException {
		transactioninit();
		return transactionVal(beginTransaction);
	}
	
	private boolean transactionVal(String action) throws JMSException{
		Response response;
		try {
			response = client.request(JMSTransactionServlet.SERVICE_NAME, action, 0);
		} catch (IOException e) {
			throw new JMSException(e.getMessage(), e);
		}
		
		RESMessage message = RESMessageDecoder.decode(response.getContent());
		if (message.getCode() == 0) {
			return true;
		}else{
			throw new JMSException(message.getDescription());
		}
		
	}

	public boolean commit() throws JMSException {
		transactioninit();
		return transactionVal(commit);
	}

	public boolean rollback() throws JMSException {
		transactioninit();
		return transactionVal(rollback);
	}

	public Message revice() throws JMSException {
		Response response;
		try {
			response = client.request(JMSConsumerServlet.SERVICE_NAME, reviceParam, 0);
		} catch (IOException e) {
			throw new JMSException(e.getMessage(), e);
		}

		return MessageDecoder.decode(response);
	}

	public Message subscibe() throws JMSException {
		Response response;
		try {
			response = client.request(JMSConsumerServlet.SERVICE_NAME, subscibeParam, 0);
		} catch (IOException e) {
			throw new JMSException(e.getMessage(), e);
		}
		return MessageDecoder.decode(response);
	}

}
