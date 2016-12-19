package com.generallycloud.nio.container.jms.client.impl;

import java.io.IOException;

import com.alibaba.fastjson.JSONObject;
import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.nio.common.ByteUtil;
import com.generallycloud.nio.container.FixedSession;
import com.generallycloud.nio.container.jms.MQException;
import com.generallycloud.nio.container.jms.Message;
import com.generallycloud.nio.container.jms.client.MessageBrowser;
import com.generallycloud.nio.container.jms.decode.DefaultMessageDecoder;
import com.generallycloud.nio.container.jms.decode.MessageDecoder;
import com.generallycloud.nio.container.jms.server.MQBrowserServlet;

public class DefaultMessageBrowser implements MessageBrowser {
	
	private final String SERVICE_NAME = "MQBrowserServlet";

	private MessageDecoder	messageDecoder	= new DefaultMessageDecoder();

	private FixedSession session = null;
	
	public DefaultMessageBrowser(FixedSession session) {
		this.session = session;
	}

	@Override
	public Message browser(String messageID) throws MQException {
		JSONObject param = new JSONObject();
		param.put("messageID", messageID);
		param.put("cmd", MQBrowserServlet.BROWSER);

		ProtobaseReadFuture future;
		try {
			future = session.request(SERVICE_NAME, param.toJSONString());
		} catch (IOException e) {
			throw new MQException(e.getMessage(), e);
		}

		return messageDecoder.decode(future);
	}

	@Override
	public int size() throws MQException {
		String param = "{cmd:\"0\"}";

		ProtobaseReadFuture future;
		try {
			future = session.request(SERVICE_NAME, param);
		} catch (IOException e) {
			throw new MQException(e.getMessage(), e);
		}
		return Integer.parseInt(future.getReadText());
	}

	@Override
	public boolean isOnline(String queueName) throws MQException {

		JSONObject param = new JSONObject();
		param.put("queueName", queueName);
		param.put("cmd", MQBrowserServlet.ONLINE);

		ProtobaseReadFuture future;
		try {
			future = session.request(SERVICE_NAME, param.toJSONString());
		} catch (IOException e) {
			throw new MQException(e.getMessage(), e);
		}

		return ByteUtil.isTrue(future.getReadText());
	}
}
