package test;

import java.io.IOException;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.component.protocol.nio.future.NIOReadFuture;
import com.generallycloud.nio.connector.TCPConnector;
import com.generallycloud.nio.extend.FixedSession;
import com.generallycloud.nio.extend.IOConnectorUtil;
import com.generallycloud.nio.extend.SimpleIOEventHandle;
import com.test.service.nio.TestGetPhoneNOServlet;

public class TestGetPhoneNO {
	
	
	public static void main(String[] args) throws IOException {

		String serviceKey = TestGetPhoneNOServlet.SERVICE_NAME;
		
		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		TCPConnector connector = IOConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = eventHandle.getFixedSession();

		connector.connect();

		session.login("admin", "admin100");
		
		NIOReadFuture future = session.request(serviceKey, null);
		System.out.println(future.getText());
		
		CloseUtil.close(connector);
		
	}
}
