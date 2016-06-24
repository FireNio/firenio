package test;

import java.io.IOException;

import com.gifisan.nio.client.TCPConnector;
import com.gifisan.nio.client.FixedSession;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.server.service.impl.SYSTEMStopServerServlet;

public class TestStopServer {

	public static void main(String[] args) throws IOException {
		String serviceKey = SYSTEMStopServerServlet.SERVICE_NAME;

		TCPConnector connector = ClientUtil.getClientConnector();
		
		connector.connect();

		connector.login("admin", "admin100");

		FixedSession session = connector.getClientSession();

		ReadFuture future = session.request(serviceKey, null);
		System.out.println(future.getText());

		CloseUtil.close(connector);

	}
}
