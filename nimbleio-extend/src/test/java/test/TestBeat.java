package test;

import java.io.IOException;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.PropertiesLoader;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.protocol.ReadFuture;
import com.generallycloud.nio.component.protocol.nio.future.NIOBeatFutureFactory;
import com.generallycloud.nio.connector.TCPConnector;
import com.generallycloud.nio.extend.FixedSession;
import com.generallycloud.nio.extend.IOConnectorUtil;
import com.generallycloud.nio.extend.SessionActiveSEListener;
import com.generallycloud.nio.extend.SimpleIOEventHandle;
import com.test.service.nio.TestSimpleServlet;

public class TestBeat {
	
	
	public static void main(String[] args) throws IOException, InterruptedException {

		PropertiesLoader.setBasepath("nio");

		String serviceKey = TestSimpleServlet.SERVICE_NAME;
		
		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		TCPConnector connector = IOConnectorUtil.getTCPConnector(eventHandle);
		
		connector.getContext().addSessionEventListener(new SessionActiveSEListener());
		
		connector.getContext().setBeatFutureFactory(new NIOBeatFutureFactory());
		
		connector.getContext().setSessionIdleTime(100);
		
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
