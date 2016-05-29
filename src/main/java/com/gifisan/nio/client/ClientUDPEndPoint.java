package com.gifisan.nio.client;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.DatagramChannel;

import com.gifisan.nio.component.AbstractUDPEndPoint;
import com.gifisan.nio.component.Session;

public class ClientUDPEndPoint extends AbstractUDPEndPoint {

	private ClientSession	session	= null;

	public ClientUDPEndPoint(ClientSession session, DatagramChannel channel, InetSocketAddress remote)
			throws SocketException {
		
		super(session.getContext(), channel, remote);
		this.session = session;
	}

	protected void extendClose() {
		if (session != null) {

			session.destroyImmediately();
		}
	}

	public ClientSession getTCPSession() {
		return session;
	}

	public void setTCPSession(Session session) {
		this.session = (ClientSession) session;
	}

}
