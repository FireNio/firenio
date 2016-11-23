package com.generallycloud.nio.component;

import com.generallycloud.nio.protocol.ReadFuture;

public interface IoEventHandle extends ReadFutureAcceptor {

	enum IoEventState {
		READ, HANDLE, WRITE
	}

	public abstract void exceptionCaught(Session session, ReadFuture future, Exception cause, IoEventState state);

	public abstract void futureSent(Session session, ReadFuture future);
}
