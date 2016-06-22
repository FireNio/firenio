package com.gifisan.nio.server;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;

import com.gifisan.nio.component.AbstractUDPEndPoint;
import com.gifisan.nio.component.Session;

public class ServerUDPEndPoint extends AbstractUDPEndPoint {

	private Session	session	= null;

	public ServerUDPEndPoint(NIOContext context, SelectionKey selectionKey, InetSocketAddress remote)
			throws SocketException {
		super(context, (DatagramChannel) selectionKey.channel(), remote);
	}

	/**
	 * 服务端的close不会被触发
	 */
	protected void extendClose() {

		if (session != null) {

			session.destroy();
		}
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = (Session) session;
	}
	
	

}
