package test;

import java.io.IOException;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.future.nio.NIOReadFuture;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.FixedSession;
import com.gifisan.nio.extend.SimpleIOEventHandle;

public class Test404 {

	public static void main(String[] args) throws IOException {

		String serviceKey = "22";
		
		SimpleIOEventHandle eventHandle = new SimpleIOEventHandle();

		TCPConnector connector = ClientUtil.getTCPConnector(eventHandle);

		FixedSession session = eventHandle.getFixedSession();

		connector.connect();

		NIOReadFuture future = session.request(serviceKey, null);

		System.out.println(future.getText());

		CloseUtil.close(connector);
	}
}
