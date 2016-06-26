package test;

import java.io.IOException;

import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.connector.OnReadFuture;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.ClientLauncher;
import com.gifisan.nio.extend.FixedSession;

public class TestSessionDisconnect {
	
	
	public static void main(String[] args) throws IOException {


		String serviceName = "TestSessionDisconnectServlet";
		String param = ClientUtil.getParamString();
		
		TCPConnector connector = null;
		try {
			ClientLauncher launcher = new ClientLauncher();
			
			connector = launcher.getTCPConnector();

			connector.connect();
			
			FixedSession session = launcher.getFixedSession();

			session.login("admin", "admin100");
			
			ReadFuture future = session.request(serviceName, param);
			System.out.println(future.getText());
			
			session.listen(serviceName, new OnReadFuture() {
				public void onResponse(FixedSession session, ReadFuture future) {
					System.out.println(future.getText());
				}
			});
			
			
			session.write(serviceName, param);
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
//			ThreadUtil.sleep(1000);
//			CloseUtil.close(connector);
		}
		
	
	}
}
