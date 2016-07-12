package com.gifisan.nio.extend.implementation;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.nio.future.NIOReadFuture;
import com.gifisan.nio.extend.RESMessage;
import com.gifisan.nio.extend.service.FutureAcceptorFilter;

public class DeployFilter extends FutureAcceptorFilter {

	protected void doAccept(Session session, NIOReadFuture future) throws Exception {
		future.write(RESMessage.SYSTEM_ERROR.toString());
		session.flush(future);
	}
	
}
