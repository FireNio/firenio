package com.generallycloud.nio.container.implementation;

import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.RESMessage;
import com.generallycloud.nio.container.service.FutureAcceptorFilter;
import com.generallycloud.nio.protocol.NamedReadFuture;

public class DeployFilter extends FutureAcceptorFilter {

	@Override
	protected void accept(SocketSession session, NamedReadFuture future) throws Exception {

		future.write(RESMessage.SYSTEM_ERROR.toString());
		session.flush(future);
	}

}
