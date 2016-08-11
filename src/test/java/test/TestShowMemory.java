package test;

import java.io.IOException;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.PropertiesLoader;
import com.gifisan.nio.component.protocol.nio.future.NIOReadFuture;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.FixedSession;
import com.gifisan.nio.extend.SimpleIOEventHandle;
import com.gifisan.nio.extend.implementation.SYSTEMShowMemoryServlet;

public class TestShowMemory {

	
	public static void main(String[] args) throws IOException {
		
		PropertiesLoader.setBasepath("nio");
		
		String serviceKey = SYSTEMShowMemoryServlet.SERVICE_NAME;
		
		String param = "{username:\"admin\",password:\"admin100\"}";
		
		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		TCPConnector connector = IOConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = eventHandle.getFixedSession();

		connector.connect();

		session.login("admin", "admin100");
		
		NIOReadFuture future = session.request(serviceKey, param);
		System.out.println(future.getText());
		
		CloseUtil.close(connector);
		
	}
}
