package com.gifisan.nio.plugin.jms.client.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.nio.DisconnectException;
import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.client.ListenOnReadFuture;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.plugin.jms.JMSException;
import com.gifisan.nio.plugin.jms.Message;
import com.gifisan.nio.plugin.jms.client.MessageConsumer;
import com.gifisan.nio.plugin.jms.client.OnMessage;
import com.gifisan.nio.plugin.jms.decode.DefaultMessageDecoder;
import com.gifisan.nio.plugin.jms.decode.MessageDecoder;
import com.gifisan.nio.server.RESMessage;
import com.gifisan.nio.server.RESMessageDecoder;

public class DefaultMessageConsumer extends DefaultJMSConnecton implements MessageConsumer {

	private String			parameter				= null;
	private String			queueName				= null;
	private MessageDecoder	messageDecoder			= new DefaultMessageDecoder();
	private boolean		sendReceiveCommand		= true;
	private boolean		sendSubscribeCommand	= true;

	private void initParam(String queueName, long timeout) {
		Map<String, String> param = new HashMap<String, String>();
		param.put("queueName", queueName);
		param.put("timeout", String.valueOf(timeout));
		this.parameter = JSONObject.toJSONString(param);
	}

	public DefaultMessageConsumer(ClientSession session, String queueName, long timeout) {
		super(session);
		this.initParam(queueName, timeout);
	}

	public DefaultMessageConsumer(ClientSession session, String queueName) {
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

	public Message receive() throws JMSException {

		sendReceiveCommand();

		return poll();
	}
	
	private Message poll() throws JMSException{
		
		ReadFuture future;
		
		try {
			future = session.poll(0);
		} catch (DisconnectException e) {
			throw new JMSException(e.getMessage(),e);
		}
		
		return messageDecoder.decode(future);
	}

	// TODO complete this 考虑收到失败message的处理
	// TODO cancel receive
	public void receive(OnMessage onMessage) throws JMSException {
		
		sendReceiveCommandCallback(onMessage);
	}

	public Message subscribe() throws JMSException {
		
		sendSubscribeCommand();
		
		return poll();
	}

	// TODO complete this 考虑收到失败message的处理
	// TODO cancel subscribe
	public void subscribe(OnMessage onMessage) throws JMSException {
		
		sendSubscribeCommandCallback(onMessage);
	}
	
	private void sendReceiveCommand() throws JMSException {
		if (sendReceiveCommand) {
			try {

				session.listen("JMSConsumerServlet",new ListenOnReadFuture(session));
				
				session.write("JMSConsumerServlet", parameter);

				sendReceiveCommand = false;
			} catch (IOException e) {
				throw new JMSException(e);
			}
		}
	}

	private void sendReceiveCommandCallback(OnMessage onMessage) throws JMSException {
		if (sendReceiveCommand) {
			try {
				
				session.listen("JMSConsumerServlet", new ConsumerOnReadFuture(onMessage));
				
				session.write("JMSConsumerServlet", parameter);

				sendReceiveCommand = false;
			} catch (IOException e) {
				throw new JMSException(e);
			}
		}
	}
	
	private void sendSubscribeCommand() throws JMSException {
		if (sendSubscribeCommand) {
			try {
				
				session.listen("JMSSubscribeServlet", new ListenOnReadFuture(session));
				
				session.write("JMSSubscribeServlet", parameter);
				
				sendSubscribeCommand = false;
			} catch (IOException e) {
				throw new JMSException(e);
			}

		}
	}

	private void sendSubscribeCommandCallback(OnMessage onMessage) throws JMSException {
		if (sendSubscribeCommand) {
			try {
				
				session.listen("JMSSubscribeServlet", new ConsumerOnReadFuture(onMessage));
				
				session.write("JMSSubscribeServlet", parameter);
				
				sendSubscribeCommand = false;
			} catch (IOException e) {
				throw new JMSException(e);
			}

		}
	}

	public void login(String username, String password) throws JMSException {
		if (logined) {
			return;
		}

		session.onStreamRead("JMSConsumerServlet", new ConsumerStreamAcceptor());
		session.onStreamRead("JMSSubscribeServlet", new ConsumerStreamAcceptor());

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
