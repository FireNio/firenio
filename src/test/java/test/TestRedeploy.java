package test;

import java.io.IOException;

import com.gifisan.nio.client.ClientConnector;
import com.gifisan.nio.client.ClientSesssion;
import com.gifisan.nio.client.Response;
import com.gifisan.nio.common.CloseUtil;

public class TestRedeploy {

	public static void main(String[] args) throws IOException {

		String serviceKey = "RedeployServlet";

		String param = "{username:\"admin\",password:\"admin100\"}";

		ClientConnector connector = ClientUtil.getClientConnector();
		connector.connect();
		ClientSesssion session = connector.getClientSession();

		Response response = session.request(serviceKey, param);
		System.out.println(response.getContent());
		
		for (int i = 0; i < 0; i++) {
			
//			response = session.request(serviceKey, param);
			
			
		}
		

		CloseUtil.close(connector);
	}
}
