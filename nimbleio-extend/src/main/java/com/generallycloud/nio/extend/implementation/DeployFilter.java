package com.generallycloud.nio.extend.implementation;

import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.protocol.ReadFuture;
import com.generallycloud.nio.extend.RESMessage;
import com.generallycloud.nio.extend.service.FutureAcceptorFilter;

public class DeployFilter extends FutureAcceptorFilter {

	public void accept(Session session, ReadFuture future) throws Exception {
		future.write(RESMessage.SYSTEM_ERROR.toString());
		session.flush(future);
	}
	
}
