package com.generallycloud.nio.component;

import com.generallycloud.nio.protocol.ReadFuture;

public interface IoEventHandle extends ReadFutureAcceptor {

	enum IoEventState {
		READ, HANDLE, WRITE
	}

	public abstract void exceptionCaught(SocketSession session, ReadFuture future, Exception cause, IoEventState state);

	public abstract void futureSent(SocketSession session, ReadFuture future);
}
