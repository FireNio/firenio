package com.gifisan.nio.acceptor;

import java.net.SocketException;
import java.nio.channels.SelectionKey;

import com.gifisan.nio.component.AbstractTCPEndPoint;
import com.gifisan.nio.component.EndPointWriter;
import com.gifisan.nio.component.NIOContext;

public class ServerTCPEndPoint extends AbstractTCPEndPoint {

	public ServerTCPEndPoint(NIOContext context, SelectionKey selectionKey,EndPointWriter endPointWriter)
			throws SocketException {
		super(context, selectionKey, endPointWriter);
	}
}
