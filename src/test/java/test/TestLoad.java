package test;

import java.io.IOException;

import com.yoocent.mtp.client.NIOClient;
import com.yoocent.mtp.servlet.test.TestSimpleServlet;

public class TestLoad {
	
	public static void main(String[] args) throws IOException {
		String serviceKey = TestSimpleServlet.SERVICE_KEY;
		long timeout = 100000;
		NIOClient client = ClientUtil.getClient();
		String param = ClientUtil.getParamString();
		long old = System.currentTimeMillis();
		for (int i = 0; i < 3000; i++) {
			client.connect();
			client.request(serviceKey, param, timeout);
			client.close();
		}
		System.out.println("Time:"+(System.currentTimeMillis() - old));
	}
}
