package test;

import java.io.IOException;

import com.gifisan.nio.client.ClientConnector;
import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.client.OnReadFuture;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;

public class TestSimple {
	
	
	public static void main(String[] args) throws IOException {


		String serviceKey = "TestSimpleServlet";
		String param = ClientUtil.getParamString();
		
		final ClientConnector connector = ClientUtil.getClientConnector();
		
		connector.connect();
		
		ClientSession session = connector.getClientSession();
		
		ReadFuture future = session.request(serviceKey, param);
		System.out.println(future.getText());
		session.write(serviceKey, param,new OnReadFuture() {
			
			public void onResponse(Session sesssion, ReadFuture future) {
				System.out.println(future.getText());
				CloseUtil.close(connector);
			}
		});
//		response = session.request(serviceKey, param);
//		System.out.println(response.getContent());
		
		
	}
}
