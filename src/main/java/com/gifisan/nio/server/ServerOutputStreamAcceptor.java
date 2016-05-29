package com.gifisan.nio.server;

import com.gifisan.nio.component.OutputStreamAcceptor;
import com.gifisan.nio.component.ServiceAcceptor;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.IOReadFuture;

public class ServerOutputStreamAcceptor implements OutputStreamAcceptor {

	private ServiceAcceptor	serviceAcceptor	= null;

	public ServerOutputStreamAcceptor(ServerContext context) {
		this.serviceAcceptor = new ServerServiceAcceptor(context);
	}

	public void accept(Session session, IOReadFuture future) throws Exception {

		serviceAcceptor.accept(session, future);
	}

}
