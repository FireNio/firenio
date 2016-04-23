package com.gifisan.nio.client;

import com.gifisan.nio.component.ReadFuture;
import com.gifisan.nio.component.ReadFutureAcceptor;
import com.gifisan.nio.component.Session;

public class ClientReadFutureAcceptor implements ReadFutureAcceptor {

	public void accept(Session session, ReadFuture future) {

		((DefaultClientSession) session).offer(future);
	}

}
