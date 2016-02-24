package test;

import java.io.IOException;

import com.gifisan.nio.client.ClientConnector;
import com.gifisan.nio.client.ClientSesssion;
import com.gifisan.nio.client.Response;
import com.gifisan.nio.common.CloseUtil;

public class TestStopServer {

	
	public static void main(String[] args) throws IOException {
		String serviceKey = "stop-server";
		
		ClientConnector connector = ClientUtil.getClientConnector();
		connector.connect();
		ClientSesssion session = connector.getClientSession();
		
		String param = "{username:\"admin\",password:\"admin100\"}";
		
		Response response = session.request(serviceKey, param);
		System.out.println(response.getContent());
		
		CloseUtil.close(connector);
		
	}
}
