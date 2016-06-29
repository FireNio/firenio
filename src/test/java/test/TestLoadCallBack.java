package test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.connector.TCPConnector;
import com.gifisan.nio.extend.ClientLauncher;
import com.gifisan.nio.extend.FixedSession;
import com.gifisan.nio.extend.OnReadFuture;

public class TestLoadCallBack {
	
	public static int time = 200000;
	public static final CountDownLatch latch = new CountDownLatch(time);
	public static void main(String[] args) throws IOException{
		
		String serviceName = "TestSimpleServlet";
		
		ClientLauncher launcher = new ClientLauncher();
		
		TCPConnector connector = launcher.getTCPConnector();

		connector.connect();
		
		FixedSession session = launcher.getFixedSession();

		session.login("admin", "admin100");
		
		final Logger logger = LoggerFactory.getLogger(TestLoadCallBack.class);
		
		System.out.println("################## Test start ####################");
		long old = System.currentTimeMillis();
		
		
		session.listen(serviceName, new OnReadFuture() {
			public void onResponse(FixedSession session, ReadFuture future) {
				latch.countDown();
				long count = latch.getCount();
//				if (count % 10 == 0) {
				if (count < 200) {
					logger.info("************************================"+count);
				}
//				}
			}
		});
		
		for (int i = 0; i < time; i++) {
			session.write(serviceName, serviceName);
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
		CloseUtil.close(connector);
		
	}
}
