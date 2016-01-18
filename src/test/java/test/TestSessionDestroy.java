package test;

import java.io.IOException;

import com.gifisan.mtp.client.NIOClient;
import com.gifisan.mtp.client.Response;

public class TestSessionDestroy {
	
	
	public static void main(String[] args) throws IOException {

		String serviceKey = "JMSLoginServlet";
		String param = ClientUtil.getParamString();
		NIOClient client = ClientUtil.getClient();
		
		client.connect();
		Response response = client.request(serviceKey, param);
		System.out.println(response.getContent());
		response = client.request(serviceKey, param);
		client.close();
		
		System.out.println(response.getContent());
	}
}
