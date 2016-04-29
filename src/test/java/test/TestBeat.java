package test;

import java.io.IOException;

import com.gifisan.nio.client.ClientConnector;
import com.gifisan.nio.client.ClientSession;

public class TestBeat {
	
	
	public static void main(String[] args) throws IOException, InterruptedException {


		String serviceKey = "TestSimpleServlet";
		ClientConnector connector = ClientUtil.getClientConnector();
		connector.connect();
		ClientSession session = connector.getClientSession();
		String param = ClientUtil.getParamString();
		
		connector.keepAlive(10);
		long old = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			session.request(serviceKey, param);
		}
		System.out.println("Time:"+(System.currentTimeMillis() - old));
		
		connector.close();
		
	}
}
