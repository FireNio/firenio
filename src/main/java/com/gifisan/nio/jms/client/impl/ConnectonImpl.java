package com.gifisan.nio.jms.client.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.nio.client.ClientSesssion;
import com.gifisan.nio.client.Response;
import com.gifisan.nio.jms.JMSException;
import com.gifisan.nio.jms.client.JMSConnection;

public class ConnectonImpl implements JMSConnection {

	boolean		logined	= false;
	ClientSesssion	session	= null;

	public ConnectonImpl(ClientSesssion session) throws JMSException {
		this.session = session;
	}

	public void login(String username, String password) throws JMSException {
		if (logined) {
			return;
		}

		Map<String, String> param = new HashMap<String, String>();
		param.put("username", username);
		param.put("password", password);
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

	public void logout() {
		this.logined = false;
	}

}
