package com.gifisan.nio.plugin.jms.client.impl;

import java.io.IOException;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.common.ByteUtil;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.plugin.jms.JMSException;
import com.gifisan.nio.plugin.jms.Message;
import com.gifisan.nio.plugin.jms.client.MessageBrowser;
import com.gifisan.nio.plugin.jms.decode.DefaultMessageDecoder;
import com.gifisan.nio.plugin.jms.decode.MessageDecoder;
import com.gifisan.nio.plugin.jms.server.JMSBrowserServlet;

public class DefaultMessageBrowser implements MessageBrowser {
	
	private final String SERVICE_NAME = "JMSBrowserServlet";

	private MessageDecoder	messageDecoder	= new DefaultMessageDecoder();

	private ClientSession session = null;
	
	public DefaultMessageBrowser(ClientSession session) {
		this.session = session;
		this.session.onStreamRead(SERVICE_NAME, new ConsumerStreamAcceptor());
	}

	public Message browser(String messageID) throws JMSException {
		JSONObject param = new JSONObject();
		param.put("messageID", messageID);
		param.put("cmd", JMSBrowserServlet.BROWSER);

		ReadFuture future;
		try {
			future = session.request(SERVICE_NAME, param.toJSONString());
		} catch (IOException e) {
			throw new JMSException(e.getMessage(), e);
		}

		return messageDecoder.decode(future);
	}

	public int size() throws JMSException {
		String param = "{cmd:\"0\"}";

		ReadFuture future;
		try {
			future = session.request(SERVICE_NAME, param);
		} catch (IOException e) {
			throw new JMSException(e.getMessage(), e);
		}
		return Integer.parseInt(future.getText());
	}

	public boolean isOnline(String queueName) throws JMSException {

		JSONObject param = new JSONObject();
		param.put("queueName", queueName);
		param.put("cmd", JMSBrowserServlet.ONLINE);

		ReadFuture future;
		try {
			future = session.request(SERVICE_NAME, param.toJSONString());
		} catch (IOException e) {
			throw new JMSException(e.getMessage(), e);
		}

		return ByteUtil.isTrue(future.getText());
	}
}
