package com.generallycloud.nio.acceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.LoggerUtil;
import com.generallycloud.nio.common.ReleaseUtil;
import com.generallycloud.nio.component.AbstractChannelService;
import com.generallycloud.nio.component.BaseContext;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.SessionMEvent;
import com.generallycloud.nio.configuration.ServerConfiguration;
import com.generallycloud.nio.protocol.ChannelReadFuture;
import com.generallycloud.nio.protocol.ChannelWriteFuture;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ReadFuture;

public abstract class AbstractChannelAcceptor extends AbstractChannelService implements ChannelAcceptor {

	protected boolean		active		= false;
	protected ReentrantLock	activeLock	= new ReentrantLock();
	private Logger			logger		= LoggerFactory.getLogger(AbstractChannelAcceptor.class);

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
			
			context.setChannelService(this);

			LifeCycleUtil.start(context);

			ServerConfiguration configuration = context.getServerConfiguration();

			int SERVER_PORT = configuration.getSERVER_PORT();

			this.bind(context, getInetSocketAddress(SERVER_PORT));
			
			LoggerUtil.prettyNIOServerLog(logger, "监听已启动 @{}",getServiceDescription());

			active = true;

		} finally {

			lock.unlock();
		}
	}

	protected abstract void bind(BaseContext context, InetSocketAddress socketAddress) throws IOException;

	public void broadcast(final ReadFuture future) {

		offerSessionMEvent(new SessionMEvent() {

			public void fire(BaseContext context, Map<Integer, Session> sessions) {
				
				Iterator<Session> ss = sessions.values().iterator();
				
				Session session = ss.next();
				
				if (sessions.size() == 1) {
					
					session.flush(future);
					
					return;
				}
				
				ProtocolEncoder encoder = context.getProtocolEncoder();
				
				ChannelWriteFuture writeFuture;
				try {
					writeFuture = encoder.encode(session, (ChannelReadFuture) future);
				} catch (IOException e) {
					logger.error(e.getMessage(),e);
					return;
				}
				
				for (; ss.hasNext();) {

					Session s = ss.next();

					ChannelWriteFuture copy = writeFuture.duplicate();

					s.flush(copy);

				}
				
				ReleaseUtil.release(writeFuture);
			}
		});
	}

	protected InetSocketAddress getInetSocketAddress(int port) {
		return new InetSocketAddress(port);
	}

	public boolean isActive() {
		return active;
	}

	public void offerSessionMEvent(SessionMEvent event) {
		context.getSessionManager().offerSessionMEvent(event);
	}
	
	public int getManagedSessionSize() {
		return context.getSessionManager().getManagedSessionSize();
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

	protected abstract void unbind(BaseContext context);

}
