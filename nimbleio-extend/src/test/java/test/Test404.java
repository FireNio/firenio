package test;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.PropertiesLoader;
import com.generallycloud.nio.component.protocol.nio.future.NIOReadFuture;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.extend.FixedSession;
import com.generallycloud.nio.extend.IOConnectorUtil;
import com.generallycloud.nio.extend.SimpleIOEventHandle;

public class Test404 {

	public static void main(String[] args) throws Exception {
		
		PropertiesLoader.setBasepath("nio");

		String serviceKey = "22";
		
		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		SocketChannelConnector connector = IOConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = eventHandle.getFixedSession();

		connector.connect();

		NIOReadFuture future = session.request(serviceKey, null);

		System.out.println(future.getText());

		CloseUtil.close(connector);
	}
}
