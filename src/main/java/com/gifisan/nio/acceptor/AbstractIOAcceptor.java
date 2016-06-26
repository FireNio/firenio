package com.gifisan.nio.acceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.util.concurrent.atomic.AtomicBoolean;

import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.component.AbstractIOService;
import com.gifisan.nio.extend.configuration.ServerConfiguration;

public abstract class AbstractIOAcceptor extends AbstractIOService implements IOAcceptor{

	protected Selector		selector	;
	protected AtomicBoolean	binded	= new AtomicBoolean(false);

	protected abstract void bind(InetSocketAddress socketAddress) throws IOException;

	public void bind() throws IOException {

		if (binded.compareAndSet(false, true)) {

			if (context == null) {
				throw new IllegalArgumentException("null nio context");
			}
			
			if (context.isStopped() == true) {
				try {
					context.start();
				} catch (Exception e) {
					throw new IOException(e);
				}
			}

			ServerConfiguration configuration = context.getServerConfiguration();

			int SERVER_PORT = getSERVER_PORT(configuration);

			this.bind(getInetSocketAddress(SERVER_PORT));

			this.startComponent(context, selector);
		}
	}

	public void unbind() {
		if (binded.compareAndSet(true, false)) {

			stopComponent(context, selector);

			LifeCycleUtil.stop(context);
		}
	}

	protected abstract int getSERVER_PORT(ServerConfiguration configuration);

	protected InetSocketAddress getInetSocketAddress(int port) {
		return new InetSocketAddress(port);
	}
}
