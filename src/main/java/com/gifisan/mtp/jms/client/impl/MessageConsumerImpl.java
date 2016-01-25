package com.gifisan.mtp.jms.client.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.mtp.client.ClientSesssion;
import com.gifisan.mtp.client.Response;
import com.gifisan.mtp.component.RESMessage;
import com.gifisan.mtp.component.RESMessageDecoder;
import com.gifisan.mtp.jms.JMSException;
import com.gifisan.mtp.jms.Message;
import com.gifisan.mtp.jms.client.MessageConsumer;
import com.gifisan.mtp.jms.client.MessageDecoder;

public class MessageConsumerImpl extends ConnectonImpl implements MessageConsumer {

	private String	reviceParam	= null;
	private String	subscibeParam	= null;

	private void initParam(String queueName, long timeout) {
		Map<String, String> param = new HashMap<String, String>();
		param.put("queueName", queueName);
		param.put("timeout", String.valueOf(timeout));
		param.put("subscibe", "F");
		this.reviceParam = JSONObject.toJSONString(param);
		param.put("subscibe", "T");
		this.subscibeParam = JSONObject.toJSONString(param);
	}

	public MessageConsumerImpl(ClientSesssion session, String queueName, long timeout) throws JMSException {
		super(session);
		this.initParam(queueName, timeout);
	}

	public MessageConsumerImpl(ClientSesssion session, String queueName) throws JMSException {
		super(session);
		this.initParam(queueName, 0);
	}

	public boolean beginTransaction() throws JMSException {
		return transactionVal("begin");
	}

	private boolean transactionVal(String action) throws JMSException {
		Response response;
		try {
			response = session.request("JMSTransactionServlet", action);
		} catch (IOException e) {
			throw new JMSException(e.getMessage(), e);
		}

		RESMessage message = RESMessageDecoder.decode(response.getContent());
		if (message.getCode() == 0) {
			return true;
		} else {
			throw new JMSException(message.getDescription());
		}

	}

	public boolean commit() throws JMSException {
		return transactionVal("commit");
	}

	public boolean rollback() throws JMSException {
		return transactionVal("rollback");
	}

	public Message revice() throws JMSException {
		Response response;
		try {
			response = session.request("JMSConsumerServlet", reviceParam);
		} catch (IOException e) {
			throw new JMSException(e.getMessage(), e);
		}

		return MessageDecoder.decode(response);
	}

	public Message subscibe() throws JMSException {
		Response response;
		try {
			response = session.request("JMSTransactionServlet", subscibeParam);
		} catch (IOException e) {
			throw new JMSException(e.getMessage(), e);
		}
		return MessageDecoder.decode(response);
	}

	public void login(String username, String password) throws JMSException {
		if (logined) {
			return;
		}

		Map<String, Object> param = new HashMap<String, Object>();
		param.put("username", username);
		param.put("password", password);
		param.put("consumer", true);
		String paramString = JSONObject.toJSONString(param);

		Response response;
		try {
			response = session.request("JMSLoginServlet", paramString);
		} catch (IOException e) {
			throw new JMSException(e.getMessage(), e);
		}
		String result = response.getContent();
		boolean logined = "T".equals(result);
		if (!logined) {
			throw new JMSException("用户名密码错误！");
		}
	}


}
