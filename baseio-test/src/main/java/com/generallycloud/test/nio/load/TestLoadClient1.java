package com.generallycloud.test.nio.load;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import com.generallycloud.nio.codec.fixedlength.FixedLengthProtocolFactory;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.common.test.ITestThread;
import com.generallycloud.nio.common.test.ITestThreadHandle;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.extend.IOConnectorUtil;
import com.generallycloud.nio.protocol.ReadFuture;
import com.generallycloud.test.nio.common.ReadFutureFactory;

public class TestLoadClient1 extends ITestThread {

	private SocketChannelConnector	connector			= null;

	public void run() {

		int time1 = getTime();

		Session session = connector.getSession();

		for (int i = 0; i < time1; i++) {
			ReadFuture future = ReadFutureFactory.create(session, "test", session.getContext()
					.getIOEventHandleAdaptor());
			future.write("hello server !");
			try {
				session.flush(future);
			} catch (IOException e) {
				throw new Error(e);
			}
		}
	}

	public void prepare() throws Exception {

		SharedBundle.instance().loadAllProperties("nio");

		IOEventHandleAdaptor eventHandleAdaptor = new IOEventHandleAdaptor() {
			public void accept(Session session, ReadFuture future) throws Exception {
				CountDownLatch latch = getLatch();

				latch.countDown();

				 System.out.println("__________________________"+getLatch().getCount());
			}
		};

		connector = IOConnectorUtil.getTCPConnector(eventHandleAdaptor);

		BaseContext context = connector.getContext();

		ServerConfiguration configuration = context.getServerConfiguration();

		configuration.setSERVER_CORE_SIZE(1);
		
		configuration.setSERVER_MEMORY_POOL_CAPACITY_RATE(0.2);
		
//		configuration.setSERVER_HOST("192.168.0.180");

		context.setProtocolFactory(new FixedLengthProtocolFactory());

		connector.connect();
	}

	public void stop() {
		CloseUtil.close(connector);
	}

	public static void main(String[] args) throws IOException {

		SharedBundle.instance().loadAllProperties("nio");

		int time = 4 * 10000;

		int core_size = 4;

		ITestThreadHandle.doTest(TestLoadClient1.class, core_size, time / core_size);
	}
}
