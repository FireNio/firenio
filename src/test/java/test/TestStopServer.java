package test;

import java.io.IOException;

import com.gifisan.nio.client.ClientConnector;
import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.ReadFuture;

public class TestStopServer {

	
	public static void main(String[] args) throws IOException {
		String serviceKey = "stop-server";
		
		ClientConnector connector = ClientUtil.getClientConnector();
		connector.connect();
		ClientSession session = connector.getClientSession();
		
		String param = "{username:\"admin\",password:\"admin100\"}";
		
		ReadFuture future = session.request(serviceKey, param);
		System.out.println(future.getText());
		
		CloseUtil.close(connector);
		
	}
}
