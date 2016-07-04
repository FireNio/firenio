package test;

import java.io.IOException;

import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.ClientLauncher;
import com.gifisan.nio.extend.FixedSession;
import com.test.servlet.TestSimpleServlet;

public class TestBeat {
	
	
	public static void main(String[] args) throws IOException, InterruptedException {


		String serviceKey = TestSimpleServlet.SERVICE_NAME;
		
		ClientLauncher launcher = new ClientLauncher();
		
		TCPConnector connector = launcher.getTCPConnector();
		
		connector.setBeatPacket(10);

		connector.connect();
		
		FixedSession session = launcher.getFixedSession();

		session.login("admin", "admin100");
		
		String param = ClientUtil.getParamString();
		
		long old = System.currentTimeMillis();
		
		for (int i = 0; i < 10000; i++) {
		
			ReadFuture future = session.request(serviceKey, param);
//			System.out.println(future.getText());
			
		}
		System.out.println("Time:"+(System.currentTimeMillis() - old));
		
		connector.close();
		
	}
}
