package test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import com.gifisan.nio.client.ClientConnector;
import com.gifisan.nio.client.ClientSesssion;
import com.gifisan.nio.client.Response;
import com.gifisan.nio.common.CloseUtil;

public class TestMultiSession {

	static AtomicInteger no = new AtomicInteger(0);
	
	public static void main(String[] args) throws IOException, InterruptedException {

		final String serviceName = "TestSimpleServlet";

		final ClientConnector connector = ClientUtil.getClientConnector();

		connector.connect(false);
			
		for (int i =0; i < 4; i++) {
			new Thread(new Runnable() {
				public void run() {
					try {
						ClientSesssion session = connector.getClientSession();
						String s = "multi-session"+no.incrementAndGet();
						System.out.println(s+" 已发送......");
						Response response = session.request(serviceName, s);
						System.out.println(response.getContent());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).start();
		}
		
		Thread.sleep(100);

		CloseUtil.close(connector);
	}

}
