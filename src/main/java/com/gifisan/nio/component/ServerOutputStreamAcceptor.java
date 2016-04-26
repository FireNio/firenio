package com.gifisan.nio.component;

import com.gifisan.nio.server.session.ServerSession;
import com.gifisan.nio.service.ServiceAcceptor;

public class ServerOutputStreamAcceptor implements OutputStreamAcceptor {

	public void accept(Session session, IOReadFuture future) throws Exception {

		ServiceAcceptor acceptor = ((ServerSession) session).getServiceAcceptor();

		acceptor.accept(session, future);
	}

}
