package test;

import java.io.IOException;

import com.gifisan.mtp.client.NIOClient;
import com.gifisan.mtp.servlet.test.TestSimpleServlet;

public class TestLoad {
	
	public static void main(String[] args) throws IOException {
		String serviceKey = TestSimpleServlet.SERVICE_NAME;
		long timeout = 100000;
		NIOClient client = ClientUtil.getClient();
		String param = ClientUtil.getParamString();
		long old = System.currentTimeMillis();

		client.connect();
		for (int i = 0; i < 10000; i++) {
			client.request(serviceKey, param, timeout);
		}
		client.close();

		System.out.println("Time:"+(System.currentTimeMillis() - old));
	}
}
