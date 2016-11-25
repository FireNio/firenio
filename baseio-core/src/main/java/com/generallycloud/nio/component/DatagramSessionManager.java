package com.generallycloud.nio.component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

public interface DatagramSessionManager extends SessionManager {

	public abstract void putSession(DatagramSession session);

	public abstract void removeSession(DatagramSession session);

	public abstract DatagramSession getSession(InetSocketAddress remote);

	public abstract DatagramSession getSession(DatagramChannelSelectorLoop selectorLoop,
			java.nio.channels.DatagramChannel nioChannel, InetSocketAddress remote) throws IOException;

	public abstract void offerSessionMEvent(DatagramSessionManagerEvent event);

	public interface DatagramSessionManagerEvent {

		public abstract void fire(DatagramChannelContext context, Map<InetSocketAddress, DatagramSession> sessions);
	}
}
