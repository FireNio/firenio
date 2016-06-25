package test;

import java.io.IOException;

import com.gifisan.nio.client.TCPConnector;
import com.gifisan.nio.client.FixedSession;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.ClientLauncher;
import com.gifisan.nio.component.future.ReadFuture;
import com.test.servlet.TestGetPhoneNOServlet;

public class TestGetPhoneNO {
	
	
	public static void main(String[] args) throws IOException {

		String serviceKey = TestGetPhoneNOServlet.SERVICE_NAME;
		
		ClientLauncher launcher = new ClientLauncher();
		
		TCPConnector connector = launcher.getTCPConnector();

		connector.connect();
		
		FixedSession session = launcher.getFixedSession();

		session.login("admin", "admin100");
		
		ReadFuture future = session.request(serviceKey, null);
		System.out.println(future.getText());
		
		CloseUtil.close(connector);
		
	}
}
