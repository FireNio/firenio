package com.gifisan.nio.client;

import java.io.IOException;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.IOReadFuture;
import com.gifisan.nio.component.future.ReadFuture;

public class DefaultClientStreamAcceptor implements ClientStreamAcceptor {

	public void accept(ClientSession session, ReadFuture future) throws Exception {
		ProtectedClientSession clientSesssion = (ProtectedClientSession) session;
		
		
		ClientStreamAcceptor acceptor = clientSesssion.getStreamAcceptor(future.getServiceName());
		
		if (acceptor == null) {
			throw new IOException("null acceptor ,service name:"+future.getServiceName());
		}
		
		acceptor.accept(session, future);
		
	}

	public void accept(Session session, IOReadFuture future) throws Exception {
		
	}


}
