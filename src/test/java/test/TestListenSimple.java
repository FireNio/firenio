package test;

import java.io.IOException;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.PropertiesLoader;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.ReadFuture;
import com.gifisan.nio.component.protocol.nio.future.NIOReadFuture;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.FixedSession;
import com.gifisan.nio.extend.OnReadFuture;
import com.gifisan.nio.extend.SimpleIOEventHandle;

public class TestListenSimple {
	
	
	public static void main(String[] args) throws IOException {

		PropertiesLoader.setBasepath("nio");
		String serviceKey = "TestListenSimpleServlet";
		String param = "ttt";
		
		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		TCPConnector connector = IOConnectorUtil.getTCPConnector(eventHandle);

		FixedSession session = eventHandle.getFixedSession();

		connector.connect();

		NIOReadFuture future = session.request(serviceKey, param);
		System.out.println(future.getText());
		
		session.listen(serviceKey,new OnReadFuture() {
			
			public void onResponse(Session session, ReadFuture future) {
				NIOReadFuture f = (NIOReadFuture) future;
				System.out.println(f.getText());
			}
		});
		
		session.write(serviceKey, param, null);
		
		ThreadUtil.sleep(1000);
		CloseUtil.close(connector);
		
	}
}
