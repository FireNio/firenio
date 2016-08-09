package test.http11;

import java.io.IOException;

import test.ClientUtil;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.PropertiesLoader;
import com.gifisan.nio.component.ReadFutureFactory;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.http11.ClientHTTPProtocolFactory;
import com.gifisan.nio.component.protocol.http11.future.HttpReadFuture;
import com.gifisan.nio.component.protocol.http11.future.HttpRequestFuture;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.plugin.http.client.HttpClient;
import com.gifisan.nio.extend.plugin.http.client.HttpIOEventHandle;

public class TestSimpleHttp {

	public static void main(String[] args) throws IOException {
		
		PropertiesLoader.setBasepath("nio");

		HttpIOEventHandle eventHandleAdaptor = new HttpIOEventHandle();

		TCPConnector connector = ClientUtil.getTCPConnector(eventHandleAdaptor);
		
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
