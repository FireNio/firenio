package com.generallycloud.nio.container.protobase.example;

import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.nio.common.StringUtil;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.protobase.service.ProtobaseFutureAcceptorService;

public class TestSimpleServlet extends ProtobaseFutureAcceptorService{
	
	public static final String SERVICE_NAME = TestSimpleServlet.class.getSimpleName();
	
//	private Logger logger = LoggerFactory.getLogger(TestSimpleServlet.class);
	
	private TestSimple1 simple1 = new TestSimple1();
	
//	private AtomicInteger size = new AtomicInteger();

	@Override
	protected void doAccept(SocketSession session, ProtobaseReadFuture future) throws Exception {

//		accept.getAndIncrement();
		
		String test = future.getReadText();

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
