package com.generallycloud.nio.acceptor;

import java.io.IOException;

import com.generallycloud.nio.component.IOService;
import com.generallycloud.nio.component.SessionMEvent;
import com.generallycloud.nio.component.protocol.ReadFuture;

public interface IOAcceptor extends IOService {

	public abstract void bind() throws IOException;

	public abstract void unbind();

	public abstract void broadcast(ReadFuture future);
	
	public abstract void offerSessionMEvent(SessionMEvent event);
	
	public abstract int getManagedSessionSize();
}
