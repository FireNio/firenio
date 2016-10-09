package com.test.service.nio;

import com.generallycloud.nio.codec.nio.future.NIOReadFuture;
import com.generallycloud.nio.common.StringUtil;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.extend.service.NIOFutureAcceptorService;

public class TestSimpleServlet extends NIOFutureAcceptorService{
	
	public static final String SERVICE_NAME = TestSimpleServlet.class.getSimpleName();
	
//	private Logger logger = LoggerFactory.getLogger(TestSimpleServlet.class);
	
	private TestSimple1 simple1 = new TestSimple1();
	
//	private AtomicInteger size = new AtomicInteger();

	protected void doAccept(Session session, NIOReadFuture future) throws Exception {

//		accept.getAndIncrement();
		
		String test = future.getText();

		if (StringUtil.isNullOrBlank(test)) {
			test = "test";
		}
		future.write(simple1.dynamic());
		future.write(test);
		future.write("$");
		session.flush(future);
		
//		System.out.println("=============================="+size.incrementAndGet());
	}

//	private AtomicInteger sent = new AtomicInteger(1);
//	
//	private AtomicInteger accept = new AtomicInteger(0);
//	
//	public void futureSent(Session session, WriteFuture future) {
//		logger.info("sent:{}",sent.getAndIncrement());
//		logger.info("accept:{}",accept.get());
//	}

}
