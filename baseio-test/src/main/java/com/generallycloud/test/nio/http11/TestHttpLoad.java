package com.generallycloud.test.nio.http11;

import com.generallycloud.nio.codec.http11.ClientHTTPProtocolFactory;
import com.generallycloud.nio.codec.http11.HttpClient;
import com.generallycloud.nio.codec.http11.HttpIOEventHandle;
import com.generallycloud.nio.codec.http11.future.HttpReadFuture;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.test.nio.common.IoConnectorUtil;
import com.generallycloud.test.nio.common.ReadFutureFactory;
import com.generallycloud.test.test.ITest;
import com.generallycloud.test.test.ITestHandle;

public class TestHttpLoad {

	public static void main(String[] args) throws Exception {
		
		SharedBundle.instance().loadAllProperties("http");

		HttpIOEventHandle eventHandleAdaptor = new HttpIOEventHandle();

		SocketChannelConnector connector = IoConnectorUtil.getTCPConnector(eventHandleAdaptor);
		
		connector.getContext().setProtocolFactory(new ClientHTTPProtocolFactory());
		
		final SocketSession session = connector.connect();

		final HttpClient client = new HttpClient(session);
		
		ITestHandle.doTest(new ITest() {
			
			@Override
			public void test(int i) throws Exception {
				
				HttpReadFuture future = ReadFutureFactory.createHttpReadFuture(session, "/test");
				
				client.request(future);
				
			}
		}, 100000, "test-http");

		CloseUtil.close(connector);

	}
}
