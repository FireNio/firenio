package test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import com.gifisan.nio.client.ClientConnector;
import com.gifisan.nio.client.ClientResponse;
import com.gifisan.nio.client.ClientSesssion;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.ThreadUtil;

public class TestMultiSession {

	static AtomicInteger	no	= new AtomicInteger(0);

	public static void main(String[] args) throws IOException {

		final String serviceName = "TestSimpleServlet";

		final ClientConnector connector = ClientUtil.getClientConnector();

		connector.connect(true);

		for (int i = 0; i < 4; i++) {
			new Thread(new Runnable() {
				public void run() {
					try {
						ClientSesssion session = connector.getClientSession();
						String s = "multi-session" + no.incrementAndGet();
						System.out.println(s + " 已发送......");
						ClientResponse response = session.request(serviceName, s);
						System.out.println(response.getText());
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).start();
		}

		ThreadUtil.sleep(500);

		CloseUtil.close(connector);
	}

}
