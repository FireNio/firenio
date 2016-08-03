package test;

import java.util.HashMap;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.nio.Encoding;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.MD5Token;
import com.gifisan.nio.component.DefaultNIOContext;
import com.gifisan.nio.component.IOEventHandleAdaptor;
import com.gifisan.nio.component.LoggerSEListener;
import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.ConnectorCloseSEListener;
import com.gifisan.nio.extend.configuration.ServerConfiguration;

public class ClientUtil {

	public static TCPConnector getTCPConnector(IOEventHandleAdaptor ioEventHandleAdaptor) {
		return getTCPConnector(ioEventHandleAdaptor, null);

	}

	public static TCPConnector getTCPConnector(IOEventHandleAdaptor ioEventHandleAdaptor,
			ServerConfiguration configuration) {

		TCPConnector connector = null;

		try {

			connector = new TCPConnector();

			NIOContext context = new DefaultNIOContext();

			context.setServerConfiguration(configuration);

			context.setIOEventHandleAdaptor(ioEventHandleAdaptor);

			context.addSessionEventListener(new LoggerSEListener());

			context.addSessionEventListener(new ConnectorCloseSEListener(connector));

			connector.setContext(context);

			return connector;

		} catch (Throwable e) {

			LoggerFactory.getLogger(ClientUtil.class).error(e.getMessage(), e);

			CloseUtil.close(connector);

			throw new RuntimeException(e);
		}

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
