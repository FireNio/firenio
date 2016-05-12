package test;

import java.io.IOException;

import com.gifisan.nio.client.ClientTCPConnector;
import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.test.ITest;
import com.gifisan.nio.common.test.ITestHandle;

public class TestLoad {
	
	public static void main(String[] args) throws IOException{
		
		
		ClientTCPConnector connector = ClientUtil.getClientConnector();
		connector.connect();
		final ClientSession session = connector.getClientSession();
		int time = 10000;
		
		ITestHandle.doTest(new ITest() {
			public void test() throws Exception {
				session.request("TestSimpleServlet", "TestSimpleServlet");
			}
		}, time, "TestLoad");
		
		CloseUtil.close(connector);
		
	}
}
