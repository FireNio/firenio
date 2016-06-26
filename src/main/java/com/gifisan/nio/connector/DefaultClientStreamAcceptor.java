package com.gifisan.nio.connector;

import java.io.IOException;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.IOReadFuture;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.extend.FixedSession;

public class DefaultClientStreamAcceptor implements ClientStreamAcceptor {

	public void accept(FixedSession session, ReadFuture future) throws Exception {
		
		ClientStreamAcceptor acceptor = session.getStreamAcceptor(future.getServiceName());
		
		if (acceptor == null) {
			throw new IOException("null acceptor ,service name:"+future.getServiceName());
		}
		
		acceptor.accept(session, future);
		
	}

	public void accept(Session session, IOReadFuture future) throws Exception {
		
	}


}
