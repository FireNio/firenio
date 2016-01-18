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
import com.gifisan.mtp.jms.client.JMSConnection;

public class ConnectonImpl implements JMSConnection {

	private boolean		logined		= false;
	NIOClient				client		= null;
	private String			host			= null;
	private int			port			= 0;
//	private Logger			logger		= LoggerFactory.getLogger(ConnectonImpl.class);

	public ConnectonImpl(String url) throws JMSException {
		this.setServer(url);
		this.client = new NIOClient(host, port);
	}

	private void setServer(String url) throws JMSException {
		URI uri;
		try {
			uri = new URI(url);
		} catch (URISyntaxException e) {
			throw new JMSException(e.getMessage(), e);
		}
		if (!"mtp".equals(uri.getScheme())) {
			throw new JMSException("mtp scheme spuorted");

		}
		this.host = uri.getHost();
		this.port = uri.getPort();

	}

	public void connect(String username, String password) throws JMSException {
		if (logined) {
			return;
		}
		try {
			client.connect();
		} catch (IOException e) {
			throw new JMSException(e.getMessage(), e);
		}

		Map<String, String> param = new HashMap<String, String>();
		param.put("username", username);
		param.put("password", password);
		String paramString = JSONObject.toJSONString(param);

		Response response;
		try {
			response = client.request("JMSLoginServlet", paramString);
		} catch (IOException e) {
			throw new JMSException(e.getMessage(), e);
		}
		String result = response.getContent();
		boolean logined = "T".equals(result);
		if (!logined) {
			this.disconnect();
			throw new JMSException("用户名密码错误！");
		}
//		logger.info("连接服务器成功 SID:" + this.sessionID);
	}

	public void disconnect() {
		CloseUtil.close(client);
		this.logined = false;
//		logger.info("已与服务器断开连接 SID:" + this.sessionID);
	}

}
