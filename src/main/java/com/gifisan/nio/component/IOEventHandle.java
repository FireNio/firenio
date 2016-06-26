package com.gifisan.nio.component;

import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.component.future.WriteFuture;

public interface IOEventHandle extends ReadFutureAcceptor {

	public abstract void exceptionCaughtOnRead(Session session, ReadFuture future, Exception cause);

	public abstract void exceptionCaughtOnWrite(Session session, ReadFuture readFuture, WriteFuture writeFuture,
			Exception cause);

	public abstract void futureSent(Session session, WriteFuture future);
}
