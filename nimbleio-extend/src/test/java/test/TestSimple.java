package test;

import java.io.IOException;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.PropertiesLoader;
import com.generallycloud.nio.common.ThreadUtil;
import com.generallycloud.nio.component.OnReadFuture;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.protocol.ReadFuture;
import com.generallycloud.nio.component.protocol.nio.future.NIOReadFuture;
import com.generallycloud.nio.connector.TCPConnector;
import com.generallycloud.nio.extend.FixedSession;
import com.generallycloud.nio.extend.IOConnectorUtil;
import com.generallycloud.nio.extend.SimpleIOEventHandle;
import com.generallycloud.nio.extend.implementation.SYSTEMShowMemoryServlet;

public class TestSimple {
	
	
	public static void main(String[] args) throws IOException {

		PropertiesLoader.setBasepath("nio");

		String serviceKey = "TestSimpleServlet";
		
		String param = "ttt";
		
		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		TCPConnector connector = IOConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = eventHandle.getFixedSession();
		connector.connect();
		session.login("admin", "admin100");
		
		NIOReadFuture future = session.request(serviceKey, param);
		System.out.println(future.getText());
		
		session.listen(serviceKey, new OnReadFuture() {
			
			public void onResponse(Session session, ReadFuture future) {
				
				NIOReadFuture f = (NIOReadFuture) future;
				System.out.println(f.getText());
			}
		});
		
		session.write(serviceKey, param);
		
		future = session.request(SYSTEMShowMemoryServlet.SERVICE_NAME, param);
		System.out.println(future.getText());
		System.out.println("__________"+session.getSession().getSessionID());
		
//		response = session.request(serviceKey, param);
//		System.out.println(response.getContent());
		
		ThreadUtil.sleep(500);
		
		CloseUtil.close(connector);
	}
}
