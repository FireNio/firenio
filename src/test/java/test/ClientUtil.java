package test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.omg.IOP.ENCODING_CDR_ENCAPS;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.nio.Encoding;
import com.gifisan.nio.common.MD5Token;
import com.gifisan.nio.common.PropertiesLoader;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.ClientLauncher;

public class ClientUtil {

	public static TCPConnector getClientConnector() throws IOException {

		PropertiesLoader.load();

		// PropertiesLoader.storageProperties("server.properties");

		String host = "192.168.1.48";

		// host = "192.168.1.97";

		// host = "180.168.141.103";
		
		host = "localhost";

		ClientLauncher launcher = new ClientLauncher();
		
		TCPConnector connector = launcher.getTCPConnector();


		// DebugUtil.info(connector.toString());

		return connector;
		// return new ClientConnector("192.168.0.111", 8300);
	}

	public static String getParamString() {
		Map params = new HashMap();
		params.put("serviceName", "test");
		params.put("username", "admin");
		params.put("password", MD5Token.getInstance().getLongToken("admin100", Encoding.DEFAULT));
		return JSONObject.toJSONString(params);
	}

	public static Map getParamMap() {
		Map params = new HashMap();
		params.put("username", "admin");
		params.put("password", "admin100");
		return params;
	}

}
