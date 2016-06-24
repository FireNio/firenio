package com.gifisan.nio.client;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.DatagramChannel;

import com.gifisan.nio.component.AbstractUDPEndPoint;
import com.gifisan.nio.component.Session;

public class ClientUDPEndPoint extends AbstractUDPEndPoint {

	private Session	session	= null;

	public ClientUDPEndPoint(Session session, DatagramChannel channel, InetSocketAddress remote)
			throws SocketException {

		super(session.getContext(), channel, remote);
		this.session = session;
	}

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
