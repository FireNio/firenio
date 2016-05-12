package test;

import java.io.IOException;

import com.gifisan.nio.client.ClientTCPConnector;
import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.client.OnReadFuture;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.component.future.ReadFuture;

public class TestSimple {
	
	
	public static void main(String[] args) throws IOException {


		String serviceKey = "TestSimpleServlet";
		String param = ClientUtil.getParamString();
		
		final ClientTCPConnector connector = ClientUtil.getClientConnector();
		
		connector.connect();
		
		ClientSession session = connector.getClientSession();
		
		ReadFuture future = session.request(serviceKey, param);
		System.out.println(future.getText());
		
		session.listen(serviceKey, new OnReadFuture() {
			
			public void onResponse(ClientSession session, ReadFuture future) {
				System.out.println(future.getText());
			}
		});
		
		session.write(serviceKey, param);
//		response = session.request(serviceKey, param);
//		System.out.println(response.getContent());
		
		ThreadUtil.sleep(500);
		CloseUtil.close(connector);
		
	}
}
