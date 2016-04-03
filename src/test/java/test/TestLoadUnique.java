package test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import com.gifisan.nio.client.ClientConnector;
import com.gifisan.nio.client.ClientSesssion;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.test.ITest;
import com.gifisan.nio.test.ITestHandle;

public class TestLoadUnique {
	
	static AtomicInteger size = new AtomicInteger();
	
	public static void main(String[] args) throws Exception {
		
		ClientConnector connector = ClientUtil.getClientConnector();
		connector.connect();
		final ClientSesssion session = connector.getClientSession();
		
		
		ITestHandle.doTest(new ITest() {
			public void test() throws IOException {
				session.request("TestSimpleServlet", "=================");
			}
		}, 10000, "TestLoadUnique");
		
		
		CloseUtil.close(connector);
	}
}
