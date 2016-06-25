package test;

import java.io.IOException;

import com.gifisan.nio.client.TCPConnector;
import com.gifisan.nio.client.FixedSession;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.ClientLauncher;
import com.gifisan.nio.component.future.ReadFuture;

public class Test404 {
	
	
	public static void main(String[] args) throws IOException {

		String serviceKey = "22";
		String param = ClientUtil.getParamString();
		
		ClientLauncher launcher = new ClientLauncher();
		
		TCPConnector connector = launcher.getTCPConnector();

		connector.connect();
		
		FixedSession session = launcher.getFixedSession();

		ReadFuture future = session.request(serviceKey, param);
		System.out.println(future.getText());
		
		CloseUtil.close(connector);
	}
}
