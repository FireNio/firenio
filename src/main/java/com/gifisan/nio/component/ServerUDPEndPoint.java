package com.gifisan.nio.component;

import java.net.SocketException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;

import com.gifisan.nio.server.IOSession;
import com.gifisan.nio.server.NIOContext;

public class ServerUDPEndPoint extends AbstractUDPEndPoint implements UDPEndPoint {

	private SelectionKey	selectionKey	= null;
	private IOSession		session		= null;

	public ServerUDPEndPoint(NIOContext context, SelectionKey selectionKey) throws SocketException {
		super(context, (DatagramChannel) selectionKey.channel());
		this.selectionKey = selectionKey;
	}

	/**
	 * 服务端的close不会被触发
	 */
	protected void extendClose() {

		this.selectionKey.attach(null);

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
