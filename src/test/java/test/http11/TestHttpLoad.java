package test.http11;

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
import com.gifisan.nio.component.protocol.http11.ClientHTTPProtocolFactory;
import com.gifisan.nio.component.protocol.http11.future.DefaultHTTPReadFuture;
import com.gifisan.nio.component.protocol.http11.future.HTTPReadFuture;
import com.gifisan.nio.connector.TCPConnector;

public class TestHttpLoad {

	final static int		time		= 1;
	final static Logger	logger	= LoggerFactory.getLogger(TestHttpLoad.class);

	public static void main(String[] args) throws IOException {
		final CountDownLatch latch = new CountDownLatch(time);

		IOEventHandleAdaptor eventHandleAdaptor = new IOEventHandleAdaptor() {

			public void accept(Session session, ReadFuture future) throws Exception {
				HTTPReadFuture f = (HTTPReadFuture) future;
				System.out.println(f.getOutputStream());
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

		connector.getContext().setProtocolFactory(new ClientHTTPProtocolFactory());
		
		connector.connect();

		Session session = connector.getSession();

		System.out.println("################## Test start ####################");

		String hello = "GET /test HTTP/1.1\r\n"+
					"Host: localhost\r\n"+
					"Connection: keep-alive\r\n"+
					"Content-Length: 43\r\n"+
					"Content-Type: text/plain\r\n"+
					"Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8\r\n"+
					"Accept-Encoding: gzip, deflate, sdch\r\n"+
					"Accept-Language: zh-CN,zh;q=0.8\r\n\r\n";
		
		long old = System.currentTimeMillis();

		for (int i = 0; i < time; i++) {
			
			ReadFuture future = ReadFutureFactory.createHttpReadFuture(session, "/test");
			
			future.write(hello);

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
