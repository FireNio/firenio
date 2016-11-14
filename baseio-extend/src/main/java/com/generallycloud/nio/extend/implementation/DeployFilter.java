package com.generallycloud.nio.extend.implementation;

import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.extend.RESMessage;
import com.generallycloud.nio.extend.service.FutureAcceptorFilter;
import com.generallycloud.nio.protocol.NamedReadFuture;
import com.generallycloud.nio.protocol.TextReadFuture;

public class DeployFilter extends FutureAcceptorFilter {

	protected void accept(Session session, NamedReadFuture future) throws Exception {
		
		if (future instanceof TextReadFuture) {
			((TextReadFuture) future).write(RESMessage.SYSTEM_ERROR.toString());
			session.flush(future);
		}
	}
	
}
