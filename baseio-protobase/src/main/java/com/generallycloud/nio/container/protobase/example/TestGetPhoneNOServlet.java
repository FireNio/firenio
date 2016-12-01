package com.generallycloud.nio.container.protobase.example;

import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.protobase.service.BaseFutureAcceptorService;

public class TestGetPhoneNOServlet extends BaseFutureAcceptorService {
	
	public static final String SERVICE_NAME = TestGetPhoneNOServlet.class.getSimpleName();

	private String [] NOS = {"13811112222","18599991111","18599991111","13811112222"};
	
	private int index = 0;

	protected void doAccept(SocketSession session, BaseReadFuture future) throws Exception {

		String phone = NOS[index++];
		
		if (index == 4) {
			index = 0;
		}
		
		future.write(phone);
		
		session.flush(future);
	}

}
