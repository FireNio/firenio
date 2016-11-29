package com.generallycloud.test.nio.http11;

import java.io.IOException;

import com.generallycloud.nio.codec.http11.ClientHTTPProtocolFactory;
import com.generallycloud.nio.codec.http11.HttpClient;
import com.generallycloud.nio.codec.http11.HttpIOEventHandle;
import com.generallycloud.nio.codec.http11.future.HttpReadFuture;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.test.nio.common.IoConnectorUtil;
import com.generallycloud.test.nio.common.ReadFutureFactory;
import com.generallycloud.test.test.ITestThread;
import com.generallycloud.test.test.ITestThreadHandle;

public class TestHttpLoadThread extends ITestThread {

	HttpIOEventHandle		eventHandleAdaptor	= new HttpIOEventHandle();

	SocketChannelConnector	connector;

	SocketSession			session;

	HttpClient			client;

	public void run() {

		int time = getTime();

		for (int i = 0; i < time; i++) {

			HttpReadFuture future = ReadFutureFactory.createHttpReadFuture(session, "/test");

			try {

				client.request(future, 10000);

				getLatch().countDown();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void prepare() throws Exception {

		connector = IoConnectorUtil.getTCPConnector(eventHandleAdaptor);

		connector.getContext().setProtocolFactory(new ClientHTTPProtocolFactory());
		
		ServerConfiguration c = connector.getContext().getServerConfiguration();

		c.setSERVER_MEMORY_POOL_CAPACITY(128000);
		c.setSERVER_MEMORY_POOL_UNIT(128);
		c.setSERVER_PORT(18300);
		c.setSERVER_ENABLE_SSL(false);
		c.setSERVER_ENABLE_WORK_EVENT_LOOP(false);

		session = connector.connect();

		client = new HttpClient(session);
	}

	public void stop() {
		CloseUtil.close(connector);
	}

	public static void main(String[] args) throws IOException {

		SharedBundle.instance().loadAllProperties("http");

		int time = 4 * 10000;

		int core_size = 64;

		ITestThreadHandle.doTest(TestHttpLoadThread.class, core_size, time / core_size);
	}

}
