package test;

import java.io.IOException;

import com.gifisan.mtp.AbstractLifeCycleListener;
import com.gifisan.mtp.LifeCycle;
import com.gifisan.mtp.client.NIOClient;
import com.gifisan.mtp.component.BlockingQueueThreadPool;
import com.gifisan.mtp.schedule.Job;
import com.gifisan.mtp.servlet.test.TestSimpleServlet;

public class TestConcurrent {

	public static void main(String[] args) throws Exception {

		BlockingQueueThreadPool pool = new BlockingQueueThreadPool("test-concurrent", 16);

		pool.addLifeCycleListener(new TestConcurrentListener());

		pool.start();

		for (int i = 0; i < 16; i++) {
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

class T implements Job {

	String	sessionKey	= null;

	public T(String sessionKey) {
		this.sessionKey = sessionKey;
	}

	public void schedule() {
		try {
			String serviceKey = TestSimpleServlet.SERVICE_NAME;
			NIOClient client = ClientUtil.getClient();
			String param = ClientUtil.getParamString();
			client.connect();
			for (int i = 0; i < 100000; i++) {
				client.request(serviceKey, param);
			}
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}