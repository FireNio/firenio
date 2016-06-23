package com.gifisan.nio.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.util.concurrent.atomic.AtomicBoolean;

import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.component.IOAcceptor;
import com.gifisan.nio.server.configuration.ServerConfiguration;

public abstract class AbstractIOAcceptor implements IOAcceptor {

	protected Selector		selector	= null;
	protected NIOContext	context	= null;
	protected AtomicBoolean	binded	= new AtomicBoolean(false);

	protected abstract void bind(InetSocketAddress socketAddress) throws IOException;

	public void bind() throws IOException {

		if (binded.compareAndSet(false, true)) {

			if (context == null) {
				throw new IllegalArgumentException("null nio context");
			}
			
			try {
				context.start();
			} catch (Exception e) {
				throw new IOException(e);
			}

			ServerConfiguration configuration = context.getServerConfiguration();

			int SERVER_PORT = getSERVER_PORT(configuration);

			this.bind(getInetSocketAddress(SERVER_PORT));

			this.startComponent(context, selector);
		}
	}

	protected abstract void startComponent(NIOContext context, Selector selector);

	protected abstract void stopComponent(NIOContext context, Selector selector);

	protected abstract int getSERVER_PORT(ServerConfiguration configuration);

	public void unbind() {
		if (binded.compareAndSet(true, false)) {

			stopComponent(context, selector);

			LifeCycleUtil.stop(context);
		}
	}

	protected InetSocketAddress getInetSocketAddress(int port) {
		return new InetSocketAddress(port);
	}

	public NIOContext getContext() {
		return context;
	}

	public void setContext(NIOContext context) {
		this.context = context;
	}
}
