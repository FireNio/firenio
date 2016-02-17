package test;

import java.io.IOException;

import com.gifisan.mtp.client.ClientConnector;
import com.gifisan.mtp.client.ClientSesssion;
import com.gifisan.mtp.client.Response;
import com.gifisan.mtp.common.CloseUtil;

public class TestSimple {
	
	
	public static void main(String[] args) throws IOException {


		String serviceKey = "TestSimpleServlet";
		String param = ClientUtil.getParamString();
		
		ClientConnector connector = ClientUtil.getClientConnector();
		
		connector.connect();
		
		ClientSesssion session = connector.getClientSession();
		
		Response response = session.request(serviceKey, param);
		System.out.println(response.getContent());
//		response = session.request(serviceKey, param);
//		System.out.println(response.getContent());
		
		CloseUtil.close(connector);
	}
}
