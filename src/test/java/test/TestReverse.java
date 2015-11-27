package test;

import java.io.IOException;

import com.yoocent.mtp.client.NIOClient;
import com.yoocent.mtp.client.Response;
import com.yoocent.mtp.servlet.test.TestReverseServlet;
import com.yoocent.mtp.servlet.test.TestSimpleServlet;

public class TestReverse {
	
	
	public static void main(String[] args) throws IOException {


		long timeout = 999100000;

		String serviceKey = TestReverseServlet.SERVICE_KEY;
		String param = ClientUtil.getParamString();
		NIOClient client = ClientUtil.getClient();
		
		client.connect();
		client.request(serviceKey, param, timeout);
		Response response = client.request(serviceKey, param, timeout);
		client.close();
		
		System.out.println(response.getContent());
	}
}
