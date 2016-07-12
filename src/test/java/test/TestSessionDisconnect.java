package test;

import java.io.IOException;

import com.gifisan.nio.component.protocol.nio.future.NIOReadFuture;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.FixedSession;
import com.gifisan.nio.extend.OnReadFuture;
import com.gifisan.nio.extend.SimpleIOEventHandle;

public class TestSessionDisconnect {

	public static void main(String[] args) throws IOException {

		String serviceName = "TestSessionDisconnectServlet";
		
		String param = ClientUtil.getParamString();

		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		TCPConnector connector = ClientUtil.getTCPConnector(eventHandle);

		FixedSession session = eventHandle.getFixedSession();

		connector.connect();

		session.login("admin", "admin100");

		NIOReadFuture future = session.request(serviceName, param);
		System.out.println(future.getText());

		session.listen(serviceName, new OnReadFuture() {
			public void onResponse(FixedSession session, NIOReadFuture future) {
				System.out.println(future.getText());
			}
		});

		session.write(serviceName, param);

	}
}
