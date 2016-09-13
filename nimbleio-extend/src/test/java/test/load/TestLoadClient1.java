package test.load;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.PropertiesLoader;
import com.generallycloud.nio.common.test.ITestThread;
import com.generallycloud.nio.common.test.ITestThreadHandle;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.ReadFutureFactory;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.protocol.ReadFuture;
import com.generallycloud.nio.connector.TCPConnector;
import com.generallycloud.nio.extend.IOConnectorUtil;

public class TestLoadClient1 extends ITestThread{
	
	IOEventHandleAdaptor eventHandleAdaptor;
	
	TCPConnector connector;
	
	Session session;

	public void run() {
		
		int time1 = getTime();
		
		for (int i = 0; i < time1; i++) {
			ReadFuture future = ReadFutureFactory.create(session, "test",session.getContext().getIOEventHandleAdaptor());
			future.write("hello server !");
			try {
				session.flush(future);
			} catch (IOException e) {
				throw new Error(e);
			}
		}
	}

	public void prepare() throws Exception {
		eventHandleAdaptor = new IOEventHandleAdaptor() {
			public void accept(Session session, ReadFuture future) throws Exception {
				CountDownLatch latch = getLatch();
				
				latch.countDown();
				
//				System.out.println("__________________________"+getLatch().getCount());
			}
		};
		
		connector = IOConnectorUtil.getTCPConnector(eventHandleAdaptor);
		
		connector.connect();
		session = connector.getSession();
	}

	public void stop() {
		CloseUtil.close(connector);
	}

	public static void main(String[] args) throws IOException {
		
		PropertiesLoader.setBasepath("nio");
		
		int	time		= 12800;
		
		int core_size = 4;
		
		ITestThreadHandle.doTest(TestLoadClient1.class, core_size, time / core_size);
	}
}
