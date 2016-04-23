package test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import com.gifisan.nio.client.ClientConnector;
import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.test.ITest;
import com.gifisan.nio.common.test.ITestHandle;

public class TestLoad {
	
	static AtomicInteger size = new AtomicInteger();
	
	public static void main(String[] args) throws IOException{
		
		ClientConnector connector = ClientUtil.getClientConnector();
		connector.connect(true);
		final ClientSession session = connector.getClientSession();
		
		ITestHandle.doTest(new ITest() {
			
			public void test() throws Exception {
				session.request("TestSimpleServlet", "TestSimpleServlet");
			}
		}, 500, "TestLoad");
		
		
		CloseUtil.close(connector);
	}
}
