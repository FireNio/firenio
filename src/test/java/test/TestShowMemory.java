package test;

import java.io.IOException;

import com.gifisan.nio.client.ClientTCPConnector;
import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.future.ReadFuture;

public class TestShowMemory {

	
	public static void main(String[] args) throws IOException {
		String serviceKey = "ShowMemoryServlet";
		
		String param = "{username:\"admin\",password:\"admin100\"}";
		
		ClientTCPConnector connector = ClientUtil.getClientConnector();
		connector.connect();
		ClientSession session = connector.getClientSession();
		
		ReadFuture future = session.request(serviceKey, param);
		System.out.println(future.getText());
		
		CloseUtil.close(connector);
		
	}
}
