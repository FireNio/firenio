package com.gifisan.mtp.component;

import java.io.IOException;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.mtp.server.ServerEndPoint;
import com.gifisan.mtp.server.ServletContext;

public interface ProtocolDecoder {

	public abstract MTPRequestInputStream getInputStream();

	public abstract JSONObject getParameters();

	public abstract String getServiceName();

	public abstract String getSessionID();
	
	public abstract boolean isBeat();

	public abstract boolean decode(ServletContext context,ServerEndPoint endPoint) throws IOException;

}