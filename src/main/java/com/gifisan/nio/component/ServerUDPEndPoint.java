package com.gifisan.nio.component;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;

import com.gifisan.nio.server.IOSession;
import com.gifisan.nio.server.NIOContext;

public class ServerUDPEndPoint extends AbstractUDPEndPoint implements UDPEndPoint {

	private IOSession	session	= null;

	public ServerUDPEndPoint(NIOContext context, SelectionKey selectionKey, InetSocketAddress remote)
			throws SocketException {
		super(context, (DatagramChannel) selectionKey.channel(), remote);
	}

	/**
	 * 服务端的close不会被触发
	 */
	protected void extendClose() {

		if (session != null) {

			session.destroyImmediately();
		}
	}

	public IOSession getTCPSession() {
		return session;
	}

	public void setTCPSession(Session session) {
		this.session = (IOSession) session;
	}

}
