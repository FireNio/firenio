package com.gifisan.nio.acceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.AbstractIOService;
import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.component.ReadFutureFactory;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.SessionMEvent;
import com.gifisan.nio.component.protocol.future.ReadFuture;
import com.gifisan.nio.component.protocol.nio.future.NIOReadFuture;
import com.gifisan.nio.extend.configuration.ServerConfiguration;

public abstract class AbstractIOAcceptor extends AbstractIOService implements IOAcceptor {

	protected boolean		active		= false;
	protected ReentrantLock	activeLock	= new ReentrantLock();
	private Logger			logger		= LoggerFactory.getLogger(AbstractIOAcceptor.class);

	public void bind() throws IOException {

		ReentrantLock lock = this.activeLock;

		lock.lock();

		try {

			if (active) {
				return;
			}

			if (context == null) {
				throw new IllegalArgumentException("null nio context");
			}

			LifeCycleUtil.start(context);

			ServerConfiguration configuration = context.getServerConfiguration();

			int SERVER_PORT = getSERVER_PORT(configuration);

			this.setIOService(context);

			this.bind(context, getInetSocketAddress(SERVER_PORT));

			active = true;

		} finally {

			lock.unlock();
		}
	}

	protected abstract void bind(NIOContext context, InetSocketAddress socketAddress) throws IOException;

	public void broadcast(ReadFuture future) {

		final NIOReadFuture nioReadFuture = (NIOReadFuture) future;

		offerSessionMEvent(new SessionMEvent() {

			public void handle(Map<Integer, Session> sessions) {

				Iterator<Session> ss = sessions.values().iterator();

				for (; ss.hasNext();) {

					Session s = ss.next();

					NIOReadFuture f = ReadFutureFactory.create(s, nioReadFuture);

					f.write(nioReadFuture.getText());

					try {

						s.flush(f);

					} catch (Exception e) {

						logger.error(e.getMessage(), e);
					}
				}
			}
		});
	}

	protected InetSocketAddress getInetSocketAddress(int port) {
		return new InetSocketAddress(port);
	}

	protected abstract int getSERVER_PORT(ServerConfiguration configuration);

	public boolean isActive() {
		return active;
	}

	public void offerSessionMEvent(SessionMEvent event) {
		context.getSessionFactory().offerSessionMEvent(event);
	}

	public void unbind() {

		ReentrantLock lock = this.activeLock;

		lock.lock();

		try {

			if (!active) {
				return;
			}

			unbind(context);

		} finally {

			active = false;

			LifeCycleUtil.stop(context);

			lock.unlock();
		}
	}

	protected abstract void unbind(NIOContext context);

}
