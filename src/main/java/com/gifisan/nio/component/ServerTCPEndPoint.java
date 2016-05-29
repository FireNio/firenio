package com.gifisan.nio.component;

import java.net.SocketException;
import java.nio.channels.SelectionKey;

import com.gifisan.nio.server.IOSession;
import com.gifisan.nio.server.NIOContext;
import com.gifisan.nio.server.ServerSession;

public class ServerTCPEndPoint extends AbstractTCPEndPoint {

	private IOSession	session = null;

	public ServerTCPEndPoint(NIOContext context, SelectionKey selectionKey,EndPointWriter endPointWriter)
			throws SocketException {
		super(context, selectionKey, endPointWriter);
		this.session = new ServerSession(this);
	}


	public IOSession getSession() {
		return session;
	}
}
