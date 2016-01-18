package test;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.mtp.client.NIOClient;

public class ClientUtil {

	public static NIOClient getClient(){
		return new NIOClient("localhost", 8300);
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
