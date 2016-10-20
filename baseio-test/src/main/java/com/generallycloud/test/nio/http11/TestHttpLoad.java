package com.generallycloud.test.nio.http11;

import com.generallycloud.nio.codec.http11.ClientHTTPProtocolFactory;
import com.generallycloud.nio.codec.http11.HttpClient;
import com.generallycloud.nio.codec.http11.HttpIOEventHandle;
import com.generallycloud.nio.codec.http11.future.HttpReadFuture;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.common.test.ITest;
import com.generallycloud.nio.common.test.ITestHandle;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.extend.IOConnectorUtil;
import com.generallycloud.test.nio.common.ReadFutureFactory;

public class TestHttpLoad {

	public static void main(String[] args) throws Exception {
		
		SharedBundle.instance().loadAllProperties("http");

		HttpIOEventHandle eventHandleAdaptor = new HttpIOEventHandle();

		SocketChannelConnector connector = IOConnectorUtil.getTCPConnector(eventHandleAdaptor);
		
		connector.getContext().setProtocolFactory(new ClientHTTPProtocolFactory());
		
		final Session session = connector.connect();

		final HttpClient client = new HttpClient(session);
		
		ITestHandle.doTest(new ITest() {
			
			public void test(int i) throws Exception {
				
				HttpReadFuture future = ReadFutureFactory.createHttpReadFuture(session, "/test");
				
				client.request(future);
				
			}
		}, 100000, "test-http");

		CloseUtil.close(connector);

	}
}
