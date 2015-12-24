package test;

import java.io.IOException;

import com.gifisan.mtp.client.NIOClient;
import com.gifisan.mtp.client.Response;
import com.gifisan.mtp.servlet.test.TestInitializeErrorServlet;

public class TestInitializeError {
	
	
	public static void main(String[] args) throws IOException {

		long timeout = 999900000;

		String serviceKey = TestInitializeErrorServlet.SERVICE_NAME;
		String param = ClientUtil.getParamString();
		NIOClient client = ClientUtil.getClient();
		
		client.connect();
		Response response = client.request(serviceKey, param, timeout);
		client.close();
		
		System.out.println(response.getContent());
	}
}
