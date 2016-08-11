package test;

import java.io.IOException;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.protocol.nio.future.NIOReadFuture;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.FixedSession;
import com.gifisan.nio.extend.SimpleIOEventHandle;

public class TestException {
	
	
	public static void main(String[] args) throws IOException {

		String serviceKey = "TestExceptionServlet";
		String param = "ttt";
		
		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		TCPConnector connector = IOConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = eventHandle.getFixedSession();

		connector.connect();

		NIOReadFuture future = session.request(serviceKey, param);
		System.out.println(future.getText());
		
		CloseUtil.close(connector);
	}
}
