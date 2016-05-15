package com.gifisan.nio.plugin.rtp.client;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.plugin.jms.TextMessage;
import com.gifisan.nio.plugin.jms.client.MessageProducer;
import com.gifisan.nio.plugin.jms.client.impl.DefaultMessageProducer;
import com.gifisan.nio.plugin.rtp.RTPException;
import com.gifisan.nio.plugin.rtp.server.RTPCreateRoomServlet;
import com.gifisan.nio.plugin.rtp.server.RTPLoginServlet;

public class DefaultMessageBrowser {

	private boolean		logined	= false;
	private ClientSession	session	= null;

	public DefaultMessageBrowser(ClientSession session) {
		this.session = session;
	}

	public boolean createRoom(String customerID) throws RTPException {

		ReadFuture future;
		
		try {
			future = session.request(RTPCreateRoomServlet.SERVICE_NAME, null);
		} catch (IOException e) {
			throw new RTPException(e.getMessage(), e);
		}
		
		boolean result = "T".equals(future.getText());

		if (result) {
			this.inviteCustomer(customerID);
		}
		
		return result;
	}

	public void inviteCustomer(String customerID) {

		MessageProducer producer = new DefaultMessageProducer(session);
		
		TextMessage message = new TextMessage("msgID", customerID, text)
		
		producer.offer()
		
	}

	public void login(String username, String password) throws RTPException {
		if (logined) {
			return;
		}

		Map<String, Object> param = new HashMap<String, Object>();
		param.put("username", username);
		param.put("password", password);
		String paramString = JSONObject.toJSONString(param);

		ReadFuture future;
		try {
			future = session.request(RTPLoginServlet.SERVICE_NAME, paramString);
		} catch (IOException e) {
			throw new RTPException(e.getMessage(), e);
		}
		String result = future.getText();
		boolean logined = "T".equals(result);
		if (!logined) {
			throw new RTPException("用户名密码错误！");
		}
	}

	public void logout() {
		this.logined = false;
	}

}
