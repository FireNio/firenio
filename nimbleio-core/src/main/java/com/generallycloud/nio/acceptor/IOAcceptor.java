package com.generallycloud.nio.acceptor;

import java.io.IOException;

import com.generallycloud.nio.common.Unbindable;
import com.generallycloud.nio.component.IOService;
import com.generallycloud.nio.component.SessionMEvent;
import com.generallycloud.nio.protocol.ReadFuture;

public interface IOAcceptor extends Unbindable, IOService {

	public abstract void bind() throws IOException;

	public abstract void broadcast(ReadFuture future);
	
	public abstract void offerSessionMEvent(SessionMEvent event);
	
	public abstract int getManagedSessionSize();
}
