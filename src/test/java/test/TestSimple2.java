package test;

import java.io.IOException;

import com.yoocent.mtp.client.NIOClient;
import com.yoocent.mtp.client.Response;
import com.yoocent.mtp.servlet.test.TestSimple2Servlet;

public class TestSimple2 {
	
	
	public static void main(String[] args) throws IOException {


		long timeout = 999900000;

		String serviceKey = TestSimple2Servlet.SERVICE_KEY;
		String param = ClientUtil.getParamString();
		NIOClient client = ClientUtil.getClient();
		
		client.connect();
		Response response = client.request(serviceKey, param, timeout);
		client.close();
		
		System.out.println(response.getContent());
	}
}
