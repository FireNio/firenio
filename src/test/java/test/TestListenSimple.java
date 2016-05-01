package test;

import java.io.IOException;

import com.gifisan.nio.client.ClientConnector;
import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.client.OnReadFuture;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.component.future.ReadFuture;

public class TestListenSimple {
	
	
	public static void main(String[] args) throws IOException {


		String serviceKey = "TestListenSimpleServlet";
		String param = ClientUtil.getParamString();
		
		final ClientConnector connector = ClientUtil.getClientConnector();
		
		connector.connect();
		
		ClientSession session = connector.getClientSession();
		
		ReadFuture future = session.request(serviceKey, param);
		System.out.println(future.getText());
		
		session.listen(serviceKey, param,new OnReadFuture() {
			
			public void onResponse(ClientSession sesssion, ReadFuture future) {
				System.out.println(future.getText());
			}
		});
		
		ThreadUtil.sleep(1000);
		CloseUtil.close(connector);
		
	}
}
