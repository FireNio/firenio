package com.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.nio.client.TCPConnector;
import com.gifisan.nio.common.PropertiesLoader;
import com.gifisan.nio.common.SharedBundle;

public class ClientUtil {

	public static TCPConnector getClientConnector() throws IOException {

		PropertiesLoader.load();

		// PropertiesLoader.storageProperties("server.properties");

		String host = "192.168.1.48";

		// host = "192.168.1.97";

		// host = "180.168.141.103";
		
		host = "localhost";

		TCPConnector connector = new TCPConnector();

		// DebugUtil.info(connector.toString());

		return connector;
		// return new ClientConnector("192.168.0.111", 8300);
	}

	public static String getParamString() {
		Map params = new HashMap();
		params.put("serviceName", "test");
		params.put("username", "admin");
		params.put("password", "admin100");
		return JSONObject.toJSONString(params);
	}

	public static Map getParamMap() {
		Map params = new HashMap();
		params.put("username", "admin");
		params.put("password", "admin100");
		return params;
	}

}
