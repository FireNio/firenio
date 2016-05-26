package test;

import java.io.IOException;

import com.gifisan.nio.client.ClientTCPConnector;
import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.client.OnReadFuture;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.server.service.impl.SYSTEMShowMemoryServlet;

public class TestSimple {
	
	
	public static void main(String[] args) throws IOException {


		String serviceKey = "TestSimpleServlet";
		String param = ClientUtil.getParamString();
		
		final ClientTCPConnector connector = ClientUtil.getClientConnector();
		
		connector.connect();
		
		connector.login("admin", "admin100");
		
		ClientSession session = connector.getClientSession();
		
		ReadFuture future = session.request(serviceKey, param);
		System.out.println(future.getText());
		
		session.listen(serviceKey, new OnReadFuture() {
			
			public void onResponse(ClientSession session, ReadFuture future) {
				System.out.println(future.getText());
			}
		});
		
		session.write(serviceKey, param);
		
		future = session.request(SYSTEMShowMemoryServlet.SERVICE_NAME, param);
		System.out.println(future.getText());
		
//		response = session.request(serviceKey, param);
//		System.out.println(response.getContent());
		
		ThreadUtil.sleep(500);
		CloseUtil.close(connector);
		
	}
}
