package test.load;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;

import test.ClientUtil;

import com.gifisan.nio.common.CloseUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.IOEventHandleAdaptor;
import com.gifisan.nio.component.ReadFutureFactory;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.future.ReadFuture;
import com.gifisan.nio.component.protocol.future.WriteFuture;
import com.gifisan.nio.connector.TCPConnector;

public class TestLoadClient {

	final static int	time		= 1000000;
	final static Logger	logger	= LoggerFactory.getLogger(TestLoadClient.class);

	public static void main(String[] args) throws IOException {
		final CountDownLatch latch = new CountDownLatch(time);

		IOEventHandleAdaptor eventHandleAdaptor = new IOEventHandleAdaptor() {

			public void accept(Session session, ReadFuture future) throws Exception {
				latch.countDown();
				long count = latch.getCount();
				// if (count % 10 == 0) {
				if (count < 50) {
					logger.info("************************================" + count);
				}
				// }
			}

			public void futureSent(Session session, WriteFuture future) {
				
			}
		};

		TCPConnector connector = ClientUtil.getTCPConnector(eventHandleAdaptor);

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
