package test;

import java.io.IOException;

import com.gifisan.mtp.client.NIOClient;
import com.gifisan.mtp.client.Response;
import com.gifisan.mtp.servlet.test.TestReverseServlet;
import com.gifisan.mtp.servlet.test.TestSimpleServlet;

public class TestReverse {
	
	
	public static void main(String[] args) throws IOException {


		long timeout = 999100000;

		String serviceKey = TestReverseServlet.SERVICE_NAME;
		String param = ClientUtil.getParamString();
		NIOClient client = ClientUtil.getClient();
		
		client.connect();
		client.request(serviceKey, param, timeout);
		Response response = client.request(serviceKey, param, timeout);
		client.close();
		
		System.out.println(response.getContent());
	}
}
