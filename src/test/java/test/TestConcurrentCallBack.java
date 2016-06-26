package test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;

import com.gifisan.nio.common.PropertiesLoader;
import com.gifisan.nio.component.concurrent.ExecutorThreadPool;
import com.gifisan.nio.component.concurrent.ThreadPool;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.connector.OnReadFuture;
import com.gifisan.nio.extend.ClientLauncher;
import com.gifisan.nio.extend.FixedSession;

public class TestConcurrentCallBack {
	
	static int thread = 4;
	
	static int cycle = 50000;
	
	static int time =  thread * cycle;
	
	public final static CountDownLatch	latch	= new CountDownLatch(time);

	public static void main(String[] args) throws Exception {
		
		PropertiesLoader.load();
		
		ThreadPool pool = new ExecutorThreadPool("TestConcurrentCallBack", thread);

		ClientLauncher []connectors = new ClientLauncher[thread];
		
		for (int i = 0; i < connectors.length; i++) {
			
			connectors[i] = new ClientLauncher();
			
			connectors[i].getTCPConnector().connect();
		}
		
		pool.start();

		System.out.println("################## Test start ####################");
		long old = System.currentTimeMillis();
		for (int i = 0; i < thread; i++) {
			pool.dispatch(new T(connectors[i]));
		}
		
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		long spend = (System.currentTimeMillis()-old);
		System.out.println("## Execute Time:"+time);
		System.out.println("## OP/S:"+ new BigDecimal(time*1000).divide(new BigDecimal(spend),2,BigDecimal.ROUND_HALF_UP));
		System.out.println("## Expend Time:"+spend);
		
		for (int i = 0; i < connectors.length; i++) {
			connectors[i].getTCPConnector().close();
		}
		
		pool.stop();

	}
	
	static class T implements Runnable{
		
		private ClientLauncher launcher = null;
		
		public T(ClientLauncher launcher) {
			this.launcher = launcher;
		}

		public void run() {
			try {
				
				String serviceName = "TestSimpleServlet";
				FixedSession session = launcher.getFixedSession();

				
				session.listen(serviceName, new OnReadFuture() {
					public void onResponse(FixedSession session, ReadFuture future) {
//						if (future instanceof ErrorReadFuture) {
//							System.out.println(future);
//						}
						latch.countDown();
						long count = latch.getCount();
						if (count % 1000 == 0) {
							System.out.println("************************================"+count);
						}
					}
				});
				
				for (int i = 0; i < cycle; i++) {
					session.write(serviceName, serviceName);
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
