package test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import com.gifisan.nio.client.TCPConnector;
import com.gifisan.nio.client.ConnectorSession;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.test.ITest;
import com.gifisan.nio.common.test.ITestHandle;

public class TestLoadUnique {
	
	static AtomicInteger size = new AtomicInteger();
	
	public static void main(String[] args) throws Exception {
		
		TCPConnector connector = ClientUtil.getClientConnector();
		connector.connect();
		final ConnectorSession session = connector.getClientSession();
		
		
		ITestHandle.doTest(new ITest() {
			public void test() throws IOException {
				session.request("TestSimpleServlet", "=================");
			}
		}, 10000, "TestLoadUnique");
		
		
		CloseUtil.close(connector);
	}
}
