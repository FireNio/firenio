package com.gifisan.nio.component;

import java.io.IOException;
import java.nio.channels.Selector;

import com.gifisan.nio.server.NIOContext;
import com.gifisan.nio.server.configuration.ServerConfiguration;

public abstract class AbstractIOService implements IOService {

	protected NIOContext	context	= null;

	public NIOContext getContext() {
		return context;
	}

	public void setContext(NIOContext context) {
		this.context = context;
	}

	protected abstract void startComponent(NIOContext context, Selector selector) throws IOException;

	protected abstract void stopComponent(NIOContext context, Selector selector);
	
	protected abstract int getSERVER_PORT(ServerConfiguration configuration);
	
	protected String getSERVER_HOST(ServerConfiguration configuration){
		return configuration.getSERVER_HOST();
	}
}
