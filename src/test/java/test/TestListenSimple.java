package test;

import java.io.IOException;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.component.protocol.nio.future.NIOReadFuture;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.FixedSession;
import com.gifisan.nio.extend.OnReadFuture;
import com.gifisan.nio.extend.SimpleIOEventHandle;

public class TestListenSimple {
	
	
	public static void main(String[] args) throws IOException {


		String serviceKey = "TestListenSimpleServlet";
		String param = ClientUtil.getParamString();
		
		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		TCPConnector connector = ClientUtil.getTCPConnector(eventHandle);

		FixedSession session = eventHandle.getFixedSession();

		connector.connect();

		NIOReadFuture future = session.request(serviceKey, param);
		System.out.println(future.getText());
		
		session.listen(serviceKey,new OnReadFuture() {
			
			public void onResponse(FixedSession session, NIOReadFuture future) {
				System.out.println(future.getText());
			}
		});
		
		session.write(serviceKey, param, null);
		
		ThreadUtil.sleep(1000);
		CloseUtil.close(connector);
		
	}
}
