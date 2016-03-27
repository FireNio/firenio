package test;

import java.io.IOException;

import com.gifisan.nio.client.ClientConnector;
import com.gifisan.nio.client.ClientResponse;
import com.gifisan.nio.client.ClientSesssion;
import com.gifisan.nio.common.CloseUtil;

public class TestSimple {
	
	
	public static void main(String[] args) throws IOException {


		String serviceKey = "TestSimpleServlet";
		String param = ClientUtil.getParamString();
		
		ClientConnector connector = ClientUtil.getClientConnector();
		
		connector.connect();
		
		ClientSesssion session = connector.getClientSession();
		
		ClientResponse response = session.request(serviceKey, param);
		System.out.println(response.getText());
//		response = session.request(serviceKey, param);
//		System.out.println(response.getContent());
		
		CloseUtil.close(connector);
	}
}
