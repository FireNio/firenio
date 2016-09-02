package test.load;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;

import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.PropertiesLoader;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.ReadFutureFactory;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.protocol.ReadFuture;
import com.generallycloud.nio.connector.TCPConnector;
import com.generallycloud.nio.extend.IOConnectorUtil;

public class TestLoadClient {

	final static int	time		= 640000;
	
	public static void main(String[] args) throws Exception {
		
		PropertiesLoader.setBasepath("nio");
		
		final Logger	logger	= LoggerFactory.getLogger(TestLoadClient.class);
		
		final CountDownLatch latch = new CountDownLatch(time);

		IOEventHandleAdaptor eventHandleAdaptor = new IOEventHandleAdaptor() {

			public void accept(Session session, ReadFuture future) throws Exception {
				latch.countDown();
//				long count = latch.getCount();
				// if (count % 10 == 0) {
//				if (count < 50) {
//					logger.info("************************================" + count);
//				}
				// }
			}
		};

		TCPConnector connector = IOConnectorUtil.getTCPConnector(eventHandleAdaptor);

		connector.connect();

		Session session = connector.getSession();

		System.out.println("################## Test start ####################");

		long old = System.currentTimeMillis();

		for (int i = 0; i < time; i++) {
			ReadFuture future = ReadFutureFactory.create(session, "test",session.getContext().getIOEventHandleAdaptor());

			future.write("hello server !");

			session.flush(future);
		}

		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		long spend = (System.currentTimeMillis() - old);
		System.out.println("## Execute Time:" + time);
		System.out.println("## OP/S:"
				+ new BigDecimal(time * 1000).divide(new BigDecimal(spend), 2, BigDecimal.ROUND_HALF_UP));
		System.out.println("## Expend Time:" + spend);

		CloseUtil.close(connector);

	}
}
