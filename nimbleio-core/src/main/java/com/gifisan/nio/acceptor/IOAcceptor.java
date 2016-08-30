package com.gifisan.nio.acceptor;

import java.io.IOException;

import com.gifisan.nio.component.IOService;
import com.gifisan.nio.component.SessionMEvent;
import com.gifisan.nio.component.protocol.ReadFuture;

public interface IOAcceptor extends IOService {

	public abstract void bind() throws IOException;

	public abstract void unbind();

	public abstract void broadcast(ReadFuture future);
	
	public abstract void offerSessionMEvent(SessionMEvent event);
}
