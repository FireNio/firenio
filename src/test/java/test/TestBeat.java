package test;

import java.io.IOException;

import com.gifisan.mtp.client.NIOClient;
import com.gifisan.mtp.servlet.test.TestSimpleServlet;

public class TestBeat {
	
	
	public static void main(String[] args) throws IOException, InterruptedException {


		String serviceKey = TestSimpleServlet.SERVICE_NAME;
		NIOClient client = ClientUtil.getClient();
		String param = ClientUtil.getParamString();
		client.connect();
		client.keepAlive(1);
		long old = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			client.request(serviceKey, param);
		}
		System.out.println("Time:"+(System.currentTimeMillis() - old));
		
		client.close();
	}
}
