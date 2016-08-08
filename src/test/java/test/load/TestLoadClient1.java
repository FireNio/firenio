package test.load;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import test.ClientUtil;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.PropertiesLoader;
import com.gifisan.nio.common.test.ITestThread;
import com.gifisan.nio.common.test.ITestThreadHandle;
import com.gifisan.nio.component.IOEventHandleAdaptor;
import com.gifisan.nio.component.ReadFutureFactory;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.future.ReadFuture;
import com.gifisan.nio.connector.TCPConnector;

public class TestLoadClient1 extends ITestThread{
	
	IOEventHandleAdaptor eventHandleAdaptor;
	
	TCPConnector connector;
	
	Session session;

	public void run() {
		
		int time1 = getTime();
		
		for (int i = 0; i < time1; i++) {
			ReadFuture future = ReadFutureFactory.create(session, "test",session.getContext().getIOEventHandleAdaptor());
			future.write("hello server !");
			session.flush(future);
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
		
		connector = ClientUtil.getTCPConnector(eventHandleAdaptor);
		
		connector.connect();
		session = connector.getSession();
	}

	public void stop() {
		CloseUtil.close(connector);
	}

	final static int	time		= 1000000;
	
	public static void main(String[] args) throws IOException {
		
		PropertiesLoader.setBasepath("nio");
		
		ITestThreadHandle.doTest(TestLoadClient1.class, 8, time / 8);
	}
}
