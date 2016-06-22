package com.gifisan.nio.server;

import java.net.SocketException;
import java.nio.channels.SelectionKey;

import com.gifisan.nio.component.AbstractTCPEndPoint;
import com.gifisan.nio.component.EndPointWriter;
import com.gifisan.nio.component.Session;

public class ServerTCPEndPoint extends AbstractTCPEndPoint {

	private Session	session = null;

	public ServerTCPEndPoint(NIOContext context, SelectionKey selectionKey,EndPointWriter endPointWriter)
			throws SocketException {
		super(context, selectionKey, endPointWriter);
		this.session = new ServerSession(this);
	}


	public Session getSession() {
		return session;
	}
}
