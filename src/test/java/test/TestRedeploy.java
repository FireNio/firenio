package test;

import java.io.IOException;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.ClientLauncher;
import com.gifisan.nio.extend.FixedSession;
import com.gifisan.nio.extend.implementation.SYSTEMRedeployServlet;

public class TestRedeploy {

	public static void main(String[] args) throws IOException {

		String serviceKey = SYSTEMRedeployServlet.SERVICE_NAME;

		String param = "{username:\"admin\",password:\"admin100\"}";

		ClientLauncher launcher = new ClientLauncher();
		
		TCPConnector connector = launcher.getTCPConnector();

		connector.connect();
		
		FixedSession session = launcher.getFixedSession();

		session.login("admin", "admin100");

		ReadFuture future = session.request(serviceKey, param);
		System.out.println(future.getText());
		
		for (int i = 0; i < 0; i++) {
			
			future = session.request(serviceKey, param);
			
			
		}
		

		CloseUtil.close(connector);
	}
}
