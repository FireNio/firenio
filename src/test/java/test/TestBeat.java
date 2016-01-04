package test;

import java.io.IOException;

import com.gifisan.mtp.client.NIOClient;
import com.gifisan.mtp.client.Response;
import com.gifisan.mtp.servlet.test.TestSimpleServlet;

public class TestBeat {
	
	
	public static void main(String[] args) throws IOException, InterruptedException {


		long timeout = 999100000;

		String serviceKey = TestSimpleServlet.SERVICE_NAME;
		String param = ClientUtil.getParamString();
//		NIOClient client = ClientUtil.getClient();
		NIOClient client = new NIOClient("localhost", 8300, "test111");
		
		client.connect();
		client.keepAlive(1000);
		Thread.sleep(5000);
		Response response = client.request(serviceKey, param, timeout);
		System.out.println(response.getContent());
		response = client.request(serviceKey, param, timeout);
		client.close();
		
		System.out.println(response.getContent());
	}
}
