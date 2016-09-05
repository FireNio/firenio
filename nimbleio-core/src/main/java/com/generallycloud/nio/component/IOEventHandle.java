package com.generallycloud.nio.component;

import com.generallycloud.nio.component.protocol.ReadFuture;

public interface IOEventHandle extends ReadFutureAcceptor {

	enum IOEventState {
		READ, HANDLE, WRITE
	}

	public abstract void exceptionCaught(Session session, ReadFuture future, Exception cause, IOEventState state);

	public abstract void futureSent(Session session, ReadFuture future);
	
	//FIXME 优化此API
	public abstract NIOContext getContext() ;

	//FIXME 优化此API
	public abstract void setContext(NIOContext context) ;
}
