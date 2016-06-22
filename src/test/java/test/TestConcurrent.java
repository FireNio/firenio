package test;

import java.io.IOException;

import com.gifisan.nio.AbstractLifeCycleListener;
import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.client.TCPConnector;
import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.concurrent.QueueThreadPool;

public class TestConcurrent {

	public static void main(String[] args) throws Exception {

		QueueThreadPool pool = new QueueThreadPool("test-concurrent", 10);

		pool.addLifeCycleListener(new TestConcurrentListener());

		pool.start();

		for (int i = 0; i < 8; i++) {
			pool.dispatch(new T(String.valueOf(i)));
		}

		pool.stop();

	}

}

class TestConcurrentListener extends AbstractLifeCycleListener {

	private long	last	= 0;

	public void lifeCycleStarted(LifeCycle lifeCycle) {
		last = System.currentTimeMillis();
	}

	public void lifeCycleStopped(LifeCycle lifeCycle) {
		long time = System.currentTimeMillis() - last;
		System.out.println("ALL-Time:" + time);
	}

}

class T implements Runnable {

	String	sessionKey	= null;

	public T(String sessionKey) {
		this.sessionKey = sessionKey;
	}

	public void run() {
		try {
			String serviceKey = "TestSimpleServlet";
			TCPConnector connector = ClientUtil.getClientConnector();
			connector.connect();
			ClientSession session = connector.getClientSession();

			for (int i = 0; i < 125000; i++) {
				session.request(serviceKey, "==================");
			}

			CloseUtil.close(connector);

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}