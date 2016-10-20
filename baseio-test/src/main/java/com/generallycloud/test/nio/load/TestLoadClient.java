package com.generallycloud.test.nio.load;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import com.generallycloud.nio.codec.nio.NIOProtocolFactory;
import com.generallycloud.nio.codec.nio.future.NIOReadFuture;
import com.generallycloud.nio.common.CloseUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.component.IOEventHandleAdaptor;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.connector.SocketChannelConnector;
import com.generallycloud.nio.extend.IOConnectorUtil;
import com.generallycloud.nio.protocol.ReadFuture;
import com.generallycloud.test.nio.common.ReadFutureFactory;

public class TestLoadClient {

	final static int	time	= 6400;

	public static void main(String[] args) throws Exception {

		SharedBundle.instance().loadAllProperties("nio");

		final Logger logger = LoggerFactory.getLogger(TestLoadClient.class);

		final CountDownLatch latch = new CountDownLatch(time);
		
		final AtomicInteger res = new AtomicInteger();
		final AtomicInteger req = new AtomicInteger();

		IOEventHandleAdaptor eventHandleAdaptor = new IOEventHandleAdaptor() {

			public void accept(Session session, ReadFuture future) throws Exception {
				latch.countDown();
				long count = latch.getCount();
//				if (count % 10 == 0) {
//					if (count < 50) {
//						logger.info("************************================" + count);
//					}
//				}
				System.out.println("res==========="+res.getAndIncrement());
			}
			
			public void futureSent(Session session, ReadFuture future) {
//				NIOReadFuture f = (NIOReadFuture) future;
//				System.out.println(f.getWriteBuffer());
				System.out.println("req======================"+req.getAndIncrement());
				
			}
		};

		SocketChannelConnector connector = IOConnectorUtil.getTCPConnector(eventHandleAdaptor);
		
		connector.getContext().setProtocolFactory(new NIOProtocolFactory());

		Session session = connector.connect();

		System.out.println("################## Test start ####################");

		long old = System.currentTimeMillis();

		for (int i = 0; i < time; i++) {
			
			NIOReadFuture future = ReadFutureFactory.create(session, "test",eventHandleAdaptor );

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
