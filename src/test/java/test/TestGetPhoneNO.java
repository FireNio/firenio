package test;

import java.io.IOException;

import com.gifisan.mtp.client.NIOClient;
import com.gifisan.mtp.client.Response;

public class TestGetPhoneNO {
	
	
	public static void main(String[] args) throws IOException {


		String serviceKey = "TestGetPhoneNOServlet";
		NIOClient client = ClientUtil.getClient();
		
		client.connect();
		Response response = client.request(serviceKey, null);
		System.out.println(response.getContent());
		client.close();
		
	}
}
