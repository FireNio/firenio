package test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.nio.client.ClientConnector;
import com.gifisan.nio.common.SharedBundle;

public class ClientUtil {

	public static ClientConnector getClientConnector() throws IOException{
		SharedBundle bundle = SharedBundle.instance();
		
		bundle.loadLog4jProperties("conf/log4j.properties");
		
		ClientConnector connector = new ClientConnector("localhost", 8300);
		
		
		
//		DebugUtil.info(connector.toString());
		
		return connector;
//		return new ClientConnector("192.168.0.111", 8300);
	}
	
	public static String getParamString(){
		Map params = new HashMap();
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
