package test;

import java.io.IOException;

import com.gifisan.nio.client.TCPConnector;
import com.gifisan.nio.client.FixedSession;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.server.service.impl.SYSTEMRedeployServlet;

public class TestRedeploy {

	public static void main(String[] args) throws IOException {

		String serviceKey = SYSTEMRedeployServlet.SERVICE_NAME;

		String param = "{username:\"admin\",password:\"admin100\"}";

		TCPConnector connector = ClientUtil.getClientConnector();
		connector.connect();
		FixedSession session = connector.getClientSession();

		ReadFuture future = session.request(serviceKey, param);
		System.out.println(future.getText());
		
		for (int i = 0; i < 0; i++) {
			
			future = session.request(serviceKey, param);
			
			
		}
		

		CloseUtil.close(connector);
	}
}
