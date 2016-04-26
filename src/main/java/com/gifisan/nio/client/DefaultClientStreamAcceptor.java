package com.gifisan.nio.client;

import java.io.IOException;

import com.gifisan.nio.component.ClientStreamAcceptor;
import com.gifisan.nio.component.IOReadFuture;
import com.gifisan.nio.component.ReadFuture;
import com.gifisan.nio.component.Session;

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
