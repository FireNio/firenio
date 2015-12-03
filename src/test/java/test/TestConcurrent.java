package test;

import java.io.IOException;

import com.yoocent.mtp.AbstractLifeCycleListener;
import com.yoocent.mtp.LifeCycle;
import com.yoocent.mtp.client.NIOClient;
import com.yoocent.mtp.client.Response;
import com.yoocent.mtp.component.BlockingQueueThreadPool;
import com.yoocent.mtp.schedule.Job;
import com.yoocent.mtp.servlet.test.TestSimpleServlet;

public class TestConcurrent {

	
	
	
	public static void main(String[] args) throws Exception {
		
		
		BlockingQueueThreadPool pool = new BlockingQueueThreadPool();
		
		pool.addLifeCycleListener(new TestConcurrentListener());
		
		pool.start();
		
		
		for (int i = 0; i < 3000; i++) {
			pool.dispatch(new T(i+""));
		}
		
		pool.stop();
		
	}
	
	
	
}


class TestConcurrentListener extends AbstractLifeCycleListener {

	private long last = 0;
	
	public void lifeCycleStarted(LifeCycle lifeCycle) {
		last = System.currentTimeMillis();
	}
	
	
	
	public void lifeCycleStopped(LifeCycle lifeCycle) {
		long time = System.currentTimeMillis() - last;
		System.out.println("Time:"+time);
	}



}



class T implements Job{

	String sessionKey = null;
	
	public T(String sessionKey){
		this.sessionKey = sessionKey;
	}
	
	
	public void run() {
		try {
			this.doJob();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void doJob() throws Exception {
		NIOClient client = new NIOClient("localhost", 8080,sessionKey);
		
		try {
			client.connect();
			Response response = client.request(TestSimpleServlet.SERVICE_NAME, ClientUtil.getParamString(), 100000);
			client.close();
//			System.out.println(response.getContent());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}