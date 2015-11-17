package test;

import java.io.IOException;

import com.yoocent.mtp.client.NIOClient;
import com.yoocent.mtp.client.Response;
import com.yoocent.mtp.servlet.impl.StopServerServlet;

public class TestStopServer {

	
	public static void main(String[] args) throws IOException {
		String serviceKey = StopServerServlet.SERVICE_KEY;
		long timeout = 991000;
		
		NIOClient client = ClientUtil.getClient();
		String params = ClientUtil.getParamString();
		
		client.connect();
		Response response = client.request(serviceKey, params, timeout);
		client.close();
		
		System.out.println(response.getContent());
		
	}
}
