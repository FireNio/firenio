package test;

import java.io.IOException;

import com.gifisan.mtp.client.NIOClient;
import com.gifisan.mtp.client.Response;

public class Test404 {
	
	
	public static void main(String[] args) throws IOException {


		String serviceKey = "22";
		String param = ClientUtil.getParamString();
		NIOClient client = ClientUtil.getClient();
		
		client.connect();
//		client.request(serviceKey, param, timeout);
		Response response = client.request(serviceKey, param);
		client.close();
		
		System.out.println(response.getContent());
	}
}
