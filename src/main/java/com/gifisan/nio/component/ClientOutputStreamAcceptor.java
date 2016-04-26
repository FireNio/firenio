package com.gifisan.nio.component;

import java.io.IOException;

import com.gifisan.nio.client.ProtectedClientSession;

public class ClientOutputStreamAcceptor implements OutputStreamAcceptor {

	public void accept(Session session, IOReadFuture future) throws Exception {

		ProtectedClientSession clientSesssion = (ProtectedClientSession) session;

		ClientStreamAcceptor acceptor = clientSesssion.getStreamAcceptor(future.getServiceName());

		if (acceptor == null) {
			throw new IOException("null acceptor ,service name:" + future.getServiceName());
		}

		acceptor.accept(clientSesssion, future);
	}

}
