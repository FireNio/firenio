package com.gifisan.nio.component;

import com.gifisan.nio.component.future.IOReadFuture;
import com.gifisan.nio.server.ServerContext;
import com.gifisan.nio.server.ServerServiceAcceptor;

public class ServerOutputStreamAcceptor implements OutputStreamAcceptor {

	private ServiceAcceptor	serviceAcceptor	= null;

	public ServerOutputStreamAcceptor(ServerContext context) {
		this.serviceAcceptor = new ServerServiceAcceptor(context);
	}

	public void accept(Session session, IOReadFuture future) throws Exception {

		serviceAcceptor.accept(session, future);
	}

}
