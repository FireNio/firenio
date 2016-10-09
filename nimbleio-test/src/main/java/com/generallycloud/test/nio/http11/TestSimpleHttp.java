package com.generallycloud.test.nio.http11;

import com.generallycloud.nio.codec.http11.ClientHTTPProtocolFactory;
import com.generallycloud.nio.codec.http11.HttpClient;
import com.generallycloud.nio.codec.http11.HttpIOEventHandle;
import com.generallycloud.nio.codec.http11.future.HttpReadFuture;
import com.generallycloud.nio.codec.http11.future.HttpRequestFuture;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.extend.IOConnectorUtil;
import com.generallycloud.test.nio.common.ReadFutureFactory;

public class TestSimpleHttp {

	public static void main(String[] args) throws Exception {
		
		SharedBundle.instance().loadAllProperties("http");

		HttpIOEventHandle eventHandleAdaptor = new HttpIOEventHandle();

		SocketChannelConnector connector = IOConnectorUtil.getTCPConnector(eventHandleAdaptor);
		
		eventHandleAdaptor.setTCPConnector(connector);

		connector.getContext().setProtocolFactory(new ClientHTTPProtocolFactory());

		connector.connect();

		Session session = connector.getSession();

		HttpClient client = eventHandleAdaptor.getHttpClient();

		HttpRequestFuture future = ReadFutureFactory.createHttpReadFuture(session, "/test");

		HttpReadFuture res = client.request(session, future, 3000);
		System.out.println();
		System.out.println(res.getBody());
		System.out.println();
		CloseUtil.close(connector);

	}
}
