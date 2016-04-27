package test;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;

import com.gifisan.nio.client.ClientConnector;
import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.client.OnReadFuture;
import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;

public class TestLoadCallBack {
	
	public static void main(String[] args) throws IOException{
		
		
		ClientConnector connector = ClientUtil.getClientConnector();
		connector.connect();
		final ClientSession session = connector.getClientSession();
		int time = 10000;
		final CountDownLatch latch = new CountDownLatch(time);
		
		System.out.println("################## Test start ####################");
		long old = System.currentTimeMillis();
		
		for (int i = 0; i < time; i++) {
			session.write("TestSimpleServlet", "TestSimpleServlet",new OnReadFuture() {
				public void onResponse(Session sesssion, ReadFuture future) {
					latch.countDown();
				}
			});
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
