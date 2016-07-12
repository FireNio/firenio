package com.gifisan.nio.acceptor;

import java.io.IOException;
import java.util.Map;

import com.gifisan.nio.component.IOService;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.future.ReadFuture;

public interface IOAcceptor extends IOService {

	public abstract void bind() throws IOException;

	public abstract void unbind();

	public abstract void broadcast(ReadFuture future);
	
	public abstract Map<Integer, Session> getReadOnlyManagedSessions();
}
