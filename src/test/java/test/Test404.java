package test;

import java.io.IOException;

import com.gifisan.nio.client.ClientConnector;
import com.gifisan.nio.client.ClientResponse;
import com.gifisan.nio.client.ClientSesssion;
import com.gifisan.nio.common.CloseUtil;

public class Test404 {
	
	
	public static void main(String[] args) throws IOException {


		String serviceKey = "22";
		String param = ClientUtil.getParamString();
		
		ClientConnector connector = ClientUtil.getClientConnector();
		connector.connect();
		ClientSesssion session = connector.getClientSession();
		
		ClientResponse response = session.request(serviceKey, param);
		System.out.println(response.getText());
		
		CloseUtil.close(connector);
	}
}
