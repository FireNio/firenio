package test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import com.gifisan.nio.client.ClientConnector;
import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.component.ReadFuture;

public class TestMultiSession {

	static AtomicInteger	no	= new AtomicInteger(0);

	public static void main(String[] args) throws IOException {

		final String serviceName = "TestSimpleServlet";

		final ClientConnector connector = ClientUtil.getClientConnector();

		connector.connect();

		for (int i = 0; i < 4; i++) {
			new Thread(new Runnable() {
				public void run() {
					try {
						ClientSession session = connector.getClientSession((byte)no.getAndDecrement());
						String s = "multi-session" + no.incrementAndGet();
						System.out.println(s + " 已发送......");
						ReadFuture future = session.request(serviceName, s);
						System.out.println(future.getText());
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
