package test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.nio.client.ClientTCPConnector;
import com.gifisan.nio.common.PropertiesLoader;

public class ClientUtil {

	public static ClientTCPConnector getClientConnector() throws IOException{
		
		PropertiesLoader.load("log4j.properties");
		
		ClientTCPConnector connector = new ClientTCPConnector("localhost", 8900);
		
//		DebugUtil.info(connector.toString());
		
		return connector;
//		return new ClientConnector("192.168.0.111", 8300);
	}
	
	public static String getParamString(){
		Map params = new HashMap();
		params.put("serviceName", "test");
		params.put("username", "admin");
		params.put("password", "admin100");
		return JSONObject.toJSONString(params);
	}

	
	public static Map getParamMap(){
		Map params = new HashMap();
		params.put("username", "admin");
		params.put("password", "admin100");
		return params;
	}

}
