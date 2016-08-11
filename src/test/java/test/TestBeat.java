package test;

import java.io.IOException;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.PropertiesLoader;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.component.protocol.future.ReadFuture;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.FixedSession;
import com.gifisan.nio.extend.SimpleIOEventHandle;
import com.test.servlet.nio.TestSimpleServlet;

public class TestBeat {
	
	
	public static void main(String[] args) throws IOException, InterruptedException {

		PropertiesLoader.setBasepath("nio");

		String serviceKey = TestSimpleServlet.SERVICE_NAME;
		
		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		TCPConnector connector = IOConnectorUtil.getTCPConnector(eventHandle);
		
		connector.getContext().setSessionIdleTime(1200);
		
		FixedSession session = eventHandle.getFixedSession();
		
		connector.connect();
		
		session.login("admin", "admin100");
		
		String param = "tttt";
		
		long old = System.currentTimeMillis();
		
		for (int i = 0; i < 5; i++) {
		
			ReadFuture future = session.request(serviceKey, param);
			System.out.println(future);
			ThreadUtil.sleep(1000);
			
		}
		System.out.println("Time:"+(System.currentTimeMillis() - old));
		
		CloseUtil.close(session);
		
		connector.close();
		
	}
}
