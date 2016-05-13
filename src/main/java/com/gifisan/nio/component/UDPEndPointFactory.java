package com.gifisan.nio.component;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.Map;

import com.gifisan.nio.server.NIOContext;

public class UDPEndPointFactory {

	private Map<SocketAddress, UDPEndPoint>		endPoints	= new HashMap<SocketAddress, UDPEndPoint>();

	public UDPEndPoint getUDPEndPoint(NIOContext context, SelectionKey selectionKey, InetSocketAddress remote)
			throws SocketException {

		UDPEndPoint endPoint = endPoints.get(remote);

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
