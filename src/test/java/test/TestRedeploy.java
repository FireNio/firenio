package test;

import java.io.IOException;

import com.gifisan.mtp.client.ClientConnector;
import com.gifisan.mtp.client.ClientSesssion;
import com.gifisan.mtp.client.Response;
import com.gifisan.mtp.common.CloseUtil;

public class TestRedeploy {

	public static void main(String[] args) throws IOException {

		String serviceKey = "RedeployServlet";

		String param = "{username:\"admin\",password:\"admin100\"}";

		ClientConnector connector = ClientUtil.getClientConnector();
		connector.connect();
		ClientSesssion session = connector.getClientSession();

		Response response = session.request(serviceKey, param);
		System.out.println(response.getContent());

		CloseUtil.close(connector);
	}
}
