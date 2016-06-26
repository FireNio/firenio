package test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.test.ITest;
import com.gifisan.nio.common.test.ITestHandle;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.ClientLauncher;
import com.gifisan.nio.extend.FixedSession;

public class TestLoadUnique {
	
	static AtomicInteger size = new AtomicInteger();
	
	public static void main(String[] args) throws Exception {
		
		ClientLauncher launcher = new ClientLauncher();
		
		TCPConnector connector = launcher.getTCPConnector();

		connector.connect();
		
		final FixedSession session = launcher.getFixedSession();

		session.login("admin", "admin100");
		
		ITestHandle.doTest(new ITest() {
			public void test() throws IOException {
				session.request("TestSimpleServlet", "=================");
			}
		}, 10000, "TestLoadUnique");
		
		
		CloseUtil.close(connector);
	}
}
