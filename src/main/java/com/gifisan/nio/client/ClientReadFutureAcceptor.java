package com.gifisan.nio.client;

import com.gifisan.nio.component.IOReadFuture;
import com.gifisan.nio.component.ReadFutureAcceptor;
import com.gifisan.nio.component.Session;

public class ClientReadFutureAcceptor implements ReadFutureAcceptor {

	public void accept(Session session, IOReadFuture future) {

		((UnpreciseClientSession) session).offer(future);
	}

}
