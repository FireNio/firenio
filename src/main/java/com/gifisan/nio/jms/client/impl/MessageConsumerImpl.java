package com.gifisan.nio.jms.client.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.component.ReadFuture;
import com.gifisan.nio.jms.JMSException;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.jms.client.MessageConsumer;
import com.gifisan.nio.jms.client.MessageDecoder;
import com.gifisan.nio.server.RESMessage;
import com.gifisan.nio.server.RESMessageDecoder;

public class MessageConsumerImpl extends JMSConnectonImpl implements MessageConsumer {

	private String	parameter	= null;
	private String	queueName	= null;

	private void initParam(String queueName, long timeout) {
		Map<String, String> param = new HashMap<String, String>();
		param.put("queueName", queueName);
		param.put("timeout", String.valueOf(timeout));
		this.parameter = JSONObject.toJSONString(param);
	}

	public MessageConsumerImpl(ClientSession session, String queueName, long timeout) throws JMSException {
		super(session);
		this.initParam(queueName, timeout);
	}

	public MessageConsumerImpl(ClientSession session, String queueName) throws JMSException {
		super(session);
		this.queueName = queueName;
		this.initParam(queueName, 0);
	}

	public boolean beginTransaction() throws JMSException {
		return transactionVal("begin");
	}

	private boolean transactionVal(String action) throws JMSException {
		ReadFuture future;
		try {
			future = session.request("JMSTransactionServlet", action);
		} catch (IOException e) {
			throw new JMSException(e.getMessage(), e);
		}

		RESMessage message = RESMessageDecoder.decode(future.getText());
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
		ReadFuture future;
		try {
			future = session.request("JMSConsumerServlet", parameter);
		} catch (IOException e) {
			throw new JMSException(e.getMessage(), e);
		}

		return MessageDecoder.decode(future);
	}

	public Message subscibe() throws JMSException {
		ReadFuture future;
		try {
			future = session.request("JMSSubscribeServlet", parameter);
		} catch (IOException e) {
			throw new JMSException(e.getMessage(), e);
		}
		return MessageDecoder.decode(future);
	}

	public void login(String username, String password) throws JMSException {
		if (logined) {
			return;
		}

		Map<String, Object> param = new HashMap<String, Object>();
		param.put("username", username);
		param.put("password", password);
		param.put("consumer", true);
		param.put("queueName", this.queueName);
		String paramString = JSONObject.toJSONString(param);

		ReadFuture future;
		try {
			future = session.request("JMSLoginServlet", paramString);
		} catch (IOException e) {
			throw new JMSException(e.getMessage(), e);
		}
		String result = future.getText();
		boolean logined = "T".equals(result);
		if (!logined) {
			throw new JMSException("用户名密码错误！");
		}
	}

}
