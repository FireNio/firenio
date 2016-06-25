package test;

import java.io.IOException;

import com.gifisan.nio.client.TCPConnector;
import com.gifisan.nio.client.FixedSession;
import com.gifisan.nio.client.OnReadFuture;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.ThreadUtil;
import com.gifisan.nio.component.ClientLauncher;
import com.gifisan.nio.component.future.ReadFuture;

public class TestPutSession2Factory {
	
	
	public static void main(String[] args) throws IOException {


		String serviceKey = "PutSession2FactoryServlet";
		String param = ClientUtil.getParamString();
		
		ClientLauncher launcher = new ClientLauncher();
		
		TCPConnector connector = launcher.getTCPConnector();

		connector.connect();
		
		FixedSession session = launcher.getFixedSession();

		session.login("admin", "admin100");
		
		ReadFuture future = session.request(serviceKey, param);
		System.out.println(future.getText());
		
		session.listen(serviceKey, new OnReadFuture() {
			public void onResponse(FixedSession session, ReadFuture future) {
				System.out.println(future.getText());
			}
		});
		
		session.write(serviceKey, param);
//		response = session.request(serviceKey, param);
//		System.out.println(response.getContent());
		
		ThreadUtil.sleep(500);
		CloseUtil.close(connector);
		
	}
}
