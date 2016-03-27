package test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import com.gifisan.nio.client.ClientConnector;
import com.gifisan.nio.client.ClientSesssion;
import com.gifisan.nio.common.CloseUtil;

public class TestLoad {
	
	static AtomicInteger size = new AtomicInteger();
	
	public static void main(String[] args) throws IOException{
		String serviceKey = "TestSimpleServlet";
		
		ClientConnector connector = ClientUtil.getClientConnector();
		connector.connect(true);
		ClientSesssion session = connector.getClientSession();
		
		long old = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			session.request(serviceKey, serviceKey);
		}
		System.out.println("Time:"+(System.currentTimeMillis() - old));
		
		CloseUtil.close(connector);
	}
}
