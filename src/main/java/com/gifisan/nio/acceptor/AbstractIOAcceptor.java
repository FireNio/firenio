package com.gifisan.nio.acceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.AbstractIOService;
import com.gifisan.nio.component.ReadFutureFactory;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.future.ReadFuture;
import com.gifisan.nio.component.protocol.nio.future.NIOReadFuture;
import com.gifisan.nio.extend.configuration.ServerConfiguration;

public abstract class AbstractIOAcceptor extends AbstractIOService implements IOAcceptor {

	private Logger			logger	= LoggerFactory.getLogger(AbstractIOAcceptor.class);
	protected Selector		selector;
	protected AtomicBoolean	binded	= new AtomicBoolean(false);

	protected abstract void bind(InetSocketAddress socketAddress) throws IOException;

	public void bind() throws IOException {

		if (binded.compareAndSet(false, true)) {

			if (context == null) {
				throw new IllegalArgumentException("null nio context");
			}

			LifeCycleUtil.start(context);

			ServerConfiguration configuration = context.getServerConfiguration();

			int SERVER_PORT = getSERVER_PORT(configuration);

			this.bind(getInetSocketAddress(SERVER_PORT));

			this.startComponent(context, selector);

			this.setIOService(context);
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

	public void broadcast(ReadFuture future) {
		
		NIOReadFuture nioReadFuture = (NIOReadFuture) future;
		
		Map<Integer, Session> sessions = getReadOnlyManagedSessions();

		Iterator<Session> ss = sessions.values().iterator();

		for (; ss.hasNext();) {

			Session s = ss.next();

			NIOReadFuture f = ReadFutureFactory.create(s,nioReadFuture);
			
			f.write(nioReadFuture.getText());

			try {
				
				s.flush(f);
				
			} catch (Exception e) {
				
				logger.error(e.getMessage(), e);
			}
		}
	}

	public Map<Integer, Session> getReadOnlyManagedSessions() {
		return context.getSessionFactory().getReadOnlyManagedSessions();
	}

}
