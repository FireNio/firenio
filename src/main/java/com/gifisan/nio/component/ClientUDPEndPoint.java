package com.gifisan.nio.component;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.DatagramChannel;

import com.gifisan.nio.client.ClientSession;
import com.gifisan.nio.server.NIOContext;

public class ClientUDPEndPoint extends AbstractUDPEndPoint implements UDPEndPoint {

	private ClientSession	session	= null;

	public ClientUDPEndPoint(NIOContext context, DatagramChannel channel, InetSocketAddress remote)
			throws SocketException {
		super(context, channel, remote);
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
