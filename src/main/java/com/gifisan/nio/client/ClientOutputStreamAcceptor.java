package com.gifisan.nio.client;

import java.io.IOException;

import com.gifisan.nio.component.OutputStreamAcceptor;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.IOReadFuture;

public class ClientOutputStreamAcceptor implements OutputStreamAcceptor {

	public void accept(Session session, IOReadFuture future) throws Exception {

		ProtectedClientSession clientSession = (ProtectedClientSession) session;

		ClientStreamAcceptor acceptor = clientSession.getStreamAcceptor(future.getServiceName());

		if (acceptor == null) {
			throw new IOException("null acceptor ,service name:" + future.getServiceName());
		}

		acceptor.accept(clientSession, future);
	}

}
