package test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import com.gifisan.mtp.client.ClientConnector;
import com.gifisan.mtp.client.ClientSesssion;
import com.gifisan.mtp.common.CloseUtil;

public class TestLoadUnique {
	
	static AtomicInteger size = new AtomicInteger();
	
	public static void main(String[] args) throws IOException, InterruptedException {
		String serviceKey = "TestSimpleServlet";
		
		ClientConnector connector = ClientUtil.getClientConnector();
		connector.connect();
		ClientSesssion session = connector.getClientSession();
		
		long old = System.currentTimeMillis();
		for (int i = 0; i < 10000; i++) {
			session.request(serviceKey, serviceKey);
		}
		System.out.println("Time:"+(System.currentTimeMillis() - old));
		
		CloseUtil.close(connector);
	}
}
