package test;

import java.io.IOException;

import com.gifisan.mtp.client.NIOClient;

public class TestLoad {
	
	public static void main(String[] args) throws IOException {
		String serviceKey = "TestSimpleServlet";
		NIOClient client = ClientUtil.getClient();
		String param = ClientUtil.getParamString();
		client.connect();

		long old = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			client.request(serviceKey, param);
		}
		System.out.println("Time:"+(System.currentTimeMillis() - old));
		
		client.close();
	}
}
