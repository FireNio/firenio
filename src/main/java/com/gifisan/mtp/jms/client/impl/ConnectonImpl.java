package com.gifisan.mtp.jms.client.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.mtp.client.NIOClient;
import com.gifisan.mtp.client.Response;
import com.gifisan.mtp.common.CloseUtil;
import com.gifisan.mtp.jms.JMSException;
import com.gifisan.mtp.jms.client.Connection;
import com.gifisan.mtp.jms.server.JMSLoginServlet;

public class ConnectonImpl implements Connection{

	private boolean logined = false;
	
	private String sessionID = null;
	
	NIOClient client = null;
	
	private String host = null;
	
	private int port = 0;
	
	private static final String SERVICE_NAME = JMSLoginServlet.SERVICE_NAME;
	
	public ConnectonImpl(String url,String sessionID) throws JMSException {
		this.setServer(url);
		this.sessionID = sessionID;
		
		this.client = new NIOClient(host, port, sessionID);
	}
	
	private void setServer(String url) throws JMSException{
		URI uri;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			throw new JMSException(e.getMessage(),e);
		}
		if (!"mtp".equals(uri.getScheme())) {
			throw new JMSException("mtp scheme spuorted");
			
		}
		this.host = uri.getHost();
		this.port = uri.getPort();
		
	}


	public void connect(String username,String password) throws JMSException {
		if (logined) {
			return;
		}
		try {
			client.connect();
		} catch (IOException e) {
			throw new JMSException(e.getMessage(),e);
		}
		
		Map<String, String> param = new HashMap<String, String>();
		param.put("username", username);
		param.put("password", password);
		String paramString = JSONObject.toJSONString(param);
		
		Response response;
		try {
			response = client.request(SERVICE_NAME, paramString , 0);
		} catch (IOException e) {
			throw new JMSException(e.getMessage(),e);
		}
		String result = response.getContent();
		boolean logined = "T".equals(result);
		if (!logined) {
			this.disconnect();
			throw new JMSException("用户名密码错误！");
		}
		System.out.println("## 连接服务器成功！");
	}

	public String getSessionID() {
		return this.sessionID;
	}

	public void disconnect() {
		CloseUtil.close(client);
		this.logined = false;
		System.out.println("## 已与服务器断开连接");
	}
	
	
	
	
}
