package com.generallycloud.nio.container.protobase.example;

import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFutureImpl;
import com.generallycloud.nio.common.StringUtil;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.protobase.service.ProtobaseFutureAcceptorService;

public class TestListenSimpleServlet extends ProtobaseFutureAcceptorService{
	
	public static final String SERVICE_NAME = TestListenSimpleServlet.class.getSimpleName();
	
	@Override
	protected void doAccept(SocketSession session, ProtobaseReadFuture future) throws Exception {

		String test = future.getReadText();

		if (StringUtil.isNullOrBlank(test)) {
			test = "test";
		}
		
		future.write(test);
		future.write("$");
		session.flush(future);
		
		for (int i = 0; i < 5; i++) {
			
			ProtobaseReadFuture f = new ProtobaseReadFutureImpl(session.getContext(),future.getFutureID(),future.getFutureName());
			
			f.write(test);
			f.write("$");
			
			session.flush(f);
		}
		
	}

}
