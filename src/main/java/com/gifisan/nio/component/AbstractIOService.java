package com.gifisan.nio.component;

import java.nio.channels.Selector;

import com.gifisan.nio.server.NIOContext;

public abstract class AbstractIOService implements IOService {

	protected NIOContext	context	= null;

	public NIOContext getContext() {
		return context;
	}

	public void setContext(NIOContext context) {
		this.context = context;
	}

	protected abstract void startComponent(NIOContext context, Selector selector);

	protected abstract void stopComponent(NIOContext context, Selector selector);
}
