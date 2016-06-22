package com.gifisan.nio.client;

import com.gifisan.nio.component.ReadFutureAcceptor;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;

public class ClientReadFutureAcceptor implements ReadFutureAcceptor {

	public void accept(Session session, ReadFuture future) {

		((ProtectedClientSession) session).offerReadFuture(future);
	}

}
