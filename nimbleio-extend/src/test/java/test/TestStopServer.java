package test;

import java.io.IOException;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.protocol.nio.future.NIOReadFuture;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.FixedSession;
import com.gifisan.nio.extend.IOConnectorUtil;
import com.gifisan.nio.extend.SimpleIOEventHandle;
import com.gifisan.nio.extend.implementation.SYSTEMStopServerServlet;

public class TestStopServer {

	public static void main(String[] args) throws IOException {
		String serviceKey = SYSTEMStopServerServlet.SERVICE_NAME;

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
