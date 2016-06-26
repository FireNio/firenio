package test;

import java.io.IOException;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.test.ITest;
import com.gifisan.nio.common.test.ITestHandle;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.ClientLauncher;
import com.gifisan.nio.extend.FixedSession;

public class TestLoad {
	
	public static void main(String[] args) throws IOException{
		
		ClientLauncher launcher = new ClientLauncher();
		
		TCPConnector connector = launcher.getTCPConnector();

		connector.connect();
		
		final FixedSession session = launcher.getFixedSession();

		session.login("admin", "admin100");
		
		int time = 10000;
		
		ITestHandle.doTest(new ITest() {
			public void test() throws Exception {
				session.request("TestSimpleServlet", "TestSimpleServlet");
			}
		}, time, "TestLoad");
		
		CloseUtil.close(connector);
		
	}
}
