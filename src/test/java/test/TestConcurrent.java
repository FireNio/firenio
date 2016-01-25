package test;

import java.io.IOException;

import com.gifisan.mtp.AbstractLifeCycleListener;
import com.gifisan.mtp.LifeCycle;
import com.gifisan.mtp.client.ClientConnector;
import com.gifisan.mtp.client.ClientSesssion;
import com.gifisan.mtp.common.CloseUtil;
import com.gifisan.mtp.concurrent.BlockingQueueThreadPool;
import com.gifisan.mtp.schedule.Job;

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
			String serviceKey = "TestSimpleServlet";
			String param = ClientUtil.getParamString();
			ClientConnector connector = ClientUtil.getClientConnector();
			connector.connect();
			ClientSesssion session = connector.getClientSession();
			
			for (int i = 0; i < 100000; i++) {
				session.request(serviceKey, param);
			}
			
			CloseUtil.close(connector);
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}