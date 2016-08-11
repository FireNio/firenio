package test.http11;

import java.io.IOException;

import test.IOConnectorUtil;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.test.ITest;
import com.gifisan.nio.common.test.ITestHandle;
import com.gifisan.nio.component.ReadFutureFactory;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.http11.ClientHTTPProtocolFactory;
import com.gifisan.nio.component.protocol.http11.future.HttpRequestFuture;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.plugin.http.client.HttpClient;
import com.gifisan.nio.extend.plugin.http.client.HttpIOEventHandle;

public class TestHttpLoad {

	public static void main(String[] args) throws IOException {

		HttpIOEventHandle eventHandleAdaptor = new HttpIOEventHandle();

		TCPConnector connector = IOConnectorUtil.getTCPConnector(eventHandleAdaptor);
		
		eventHandleAdaptor.setTCPConnector(connector);

		connector.getContext().setProtocolFactory(new ClientHTTPProtocolFactory());
		
		connector.connect();

		final Session session = connector.getSession();

		final HttpClient client = eventHandleAdaptor.getHttpClient();
		
		ITestHandle.doTest(new ITest() {
			
			public void test() throws Exception {
				
				HttpRequestFuture future = ReadFutureFactory.createHttpReadFuture(session, "/test");
				
				client.request(session, future, 3000);
				
			}
		}, 2000, "test-http");
		

		CloseUtil.close(connector);

	}
}
