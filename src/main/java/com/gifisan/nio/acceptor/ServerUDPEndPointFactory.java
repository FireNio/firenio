package com.gifisan.nio.acceptor;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.Map;

import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.component.UDPEndPoint;
import com.gifisan.nio.component.UDPEndPointFactory;

public class ServerUDPEndPointFactory implements UDPEndPointFactory {

	private Map<SocketAddress, ServerUDPEndPoint>		endPoints	= new HashMap<SocketAddress, ServerUDPEndPoint>();

	public ServerUDPEndPoint getUDPEndPoint(NIOContext context, SelectionKey selectionKey, InetSocketAddress remote)
			throws SocketException {

		ServerUDPEndPoint endPoint = endPoints.get(remote);

		if (endPoint == null) {
			endPoint = new ServerUDPEndPoint(context, selectionKey, remote);
			selectionKey.attach(endPoint);
			endPoints.put(remote, endPoint);
		}

		return endPoint;
	}

	public void removeUDPEndPoint(UDPEndPoint endPoint) {
		endPoints.remove(endPoint.getRemoteSocketAddress());
	}
}
