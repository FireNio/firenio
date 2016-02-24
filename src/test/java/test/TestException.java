package test;

import java.io.IOException;

import com.gifisan.nio.client.ClientConnector;
import com.gifisan.nio.client.ClientSesssion;
import com.gifisan.nio.client.Response;
import com.gifisan.nio.common.CloseUtil;

public class TestException {
	
	
	public static void main(String[] args) throws IOException {

		String serviceKey = "TestExceptionServlet";
		String param = ClientUtil.getParamString();
		ClientConnector connector = ClientUtil.getClientConnector();
		connector.connect();
		ClientSesssion session = connector.getClientSession();
		
		Response response = session.request(serviceKey, param);
		System.out.println(response.getContent());
		
		CloseUtil.close(connector);
	}
}
