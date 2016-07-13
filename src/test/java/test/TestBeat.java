package test;

import java.io.IOException;

import com.gifisan.nio.component.protocol.future.ReadFuture;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.FixedSession;
import com.gifisan.nio.extend.SimpleIOEventHandle;
import com.test.servlet.nio.TestSimpleServlet;

public class TestBeat {
	
	
	public static void main(String[] args) throws IOException, InterruptedException {


		String serviceKey = TestSimpleServlet.SERVICE_NAME;
		
		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		TCPConnector connector = ClientUtil.getTCPConnector(eventHandle);

		FixedSession session = eventHandle.getFixedSession();

		connector.setBeatPacket(10);

		connector.connect();

		session.login("admin", "admin100");
		
		String param = ClientUtil.getParamString();
		
		long old = System.currentTimeMillis();
		
		for (int i = 0; i < 10000; i++) {
		
			ReadFuture future = session.request(serviceKey, param);
//			System.out.println(future.getText());
			
		}
		System.out.println("Time:"+(System.currentTimeMillis() - old));
		
		connector.close();
		
	}
}
