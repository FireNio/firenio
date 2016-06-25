package test;

import java.io.IOException;

import com.gifisan.nio.client.TCPConnector;
import com.gifisan.nio.client.FixedSession;
import com.gifisan.nio.component.ClientLauncher;
import com.test.servlet.TestSimpleServlet;

public class TestBeat {
	
	
	public static void main(String[] args) throws IOException, InterruptedException {


		String serviceKey = TestSimpleServlet.SERVICE_NAME;
		
		ClientLauncher launcher = new ClientLauncher();
		
		TCPConnector connector = launcher.getTCPConnector();

		connector.connect();
		
		FixedSession session = launcher.getFixedSession();

		session.login("admin", "admin100");
		
		String param = ClientUtil.getParamString();
		
		connector.keepAlive(10);
		
		long old = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			session.request(serviceKey, param);
		}
		System.out.println("Time:"+(System.currentTimeMillis() - old));
		
		connector.close();
		
	}
}
