package test;

import java.io.IOException;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.FixedSession;
import com.gifisan.nio.extend.SimpleIOEventHandle;
import com.test.servlet.TestGetPhoneNOServlet;

public class TestGetPhoneNO {
	
	
	public static void main(String[] args) throws IOException {

		String serviceKey = TestGetPhoneNOServlet.SERVICE_NAME;
		
		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		TCPConnector connector = ClientUtil.getTCPConnector(eventHandle);

		FixedSession session = eventHandle.getFixedSession();

		connector.connect();

		session.login("admin", "admin100");
		
		ReadFuture future = session.request(serviceKey, null);
		System.out.println(future.getText());
		
		CloseUtil.close(connector);
		
	}
}
