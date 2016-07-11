package com.gifisan.nio.extend.plugin.jms.client.impl;

import java.io.IOException;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.nio.common.ByteUtil;
import com.gifisan.nio.component.future.nio.NIOReadFuture;
import com.gifisan.nio.extend.FixedSession;
import com.gifisan.nio.extend.plugin.jms.MQException;
import com.gifisan.nio.extend.plugin.jms.Message;
import com.gifisan.nio.extend.plugin.jms.client.MessageBrowser;
import com.gifisan.nio.extend.plugin.jms.decode.DefaultMessageDecoder;
import com.gifisan.nio.extend.plugin.jms.decode.MessageDecoder;
import com.gifisan.nio.extend.plugin.jms.server.MQBrowserServlet;

public class DefaultMessageBrowser implements MessageBrowser {
	
	private final String SERVICE_NAME = "MQBrowserServlet";

	private MessageDecoder	messageDecoder	= new DefaultMessageDecoder();

	private FixedSession session = null;
	
	public DefaultMessageBrowser(FixedSession session) {
		this.session = session;
	}

	public Message browser(String messageID) throws MQException {
		JSONObject param = new JSONObject();
		param.put("messageID", messageID);
		param.put("cmd", MQBrowserServlet.BROWSER);

		NIOReadFuture future;
		try {
			future = session.request(SERVICE_NAME, param.toJSONString());
		} catch (IOException e) {
			throw new MQException(e.getMessage(), e);
		}

		return messageDecoder.decode(future);
	}

	public int size() throws MQException {
		String param = "{cmd:\"0\"}";

		NIOReadFuture future;
		try {
			future = session.request(SERVICE_NAME, param);
		} catch (IOException e) {
			throw new MQException(e.getMessage(), e);
		}
		return Integer.parseInt(future.getText());
	}

	public boolean isOnline(String queueName) throws MQException {

		JSONObject param = new JSONObject();
		param.put("queueName", queueName);
		param.put("cmd", MQBrowserServlet.ONLINE);

		NIOReadFuture future;
		try {
			future = session.request(SERVICE_NAME, param.toJSONString());
		} catch (IOException e) {
			throw new MQException(e.getMessage(), e);
		}

		return ByteUtil.isTrue(future.getText());
	}
}
