package test.http11;

import java.io.IOException;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.PropertiesLoader;
import com.generallycloud.nio.component.ReadFutureFactory;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.protocol.http11.ClientHTTPProtocolFactory;
import com.generallycloud.nio.component.protocol.http11.HttpClient;
import com.generallycloud.nio.component.protocol.http11.HttpIOEventHandle;
import com.generallycloud.nio.component.protocol.http11.future.HttpReadFuture;
import com.generallycloud.nio.component.protocol.http11.future.HttpRequestFuture;
import com.generallycloud.nio.connector.TCPConnector;
import com.generallycloud.nio.extend.IOConnectorUtil;

public class TestSimpleHttp {

	public static void main(String[] args) throws IOException {
		
		PropertiesLoader.setBasepath("nio");

		HttpIOEventHandle eventHandleAdaptor = new HttpIOEventHandle();

		TCPConnector connector = IOConnectorUtil.getTCPConnector(eventHandleAdaptor);
		
		eventHandleAdaptor.setTCPConnector(connector);

		connector.getContext().setProtocolFactory(new ClientHTTPProtocolFactory());

		connector.connect();

		Session session = connector.getSession();

		HttpClient client = eventHandleAdaptor.getHttpClient();

		HttpRequestFuture future = ReadFutureFactory.createHttpReadFuture(session, "/test");

		HttpReadFuture res = client.request(session, future, 3000);
		System.out.println();
		System.out.println(res.getOutputStream());
		System.out.println();
		CloseUtil.close(connector);

	}
}
