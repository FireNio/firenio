package com.gifisan.nio.plugin.jms.client.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.common.ByteUtil;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.plugin.jms.JMSException;
import com.gifisan.nio.plugin.jms.client.JMSConnection;

public class DefaultJMSConnecton implements JMSConnection {

	boolean		logined	= false;
	ClientSession	session	= null;

	public DefaultJMSConnecton(ClientSession session){
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

		try {
			ReadFuture future = session.request("JMSLoginServlet", paramString);
			
			String result = future.getText();
			
			if (ByteUtil.isFalse(result)) {
				
				throw new JMSException("用户名密码错误！");
			}
		} catch (IOException e) {
			throw new JMSException(e.getMessage(), e);
		}
	}

	public void logout() {
		this.logined = false;
	}

}
