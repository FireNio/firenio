package com.gifisan.nio.acceptor;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.Map;

import com.gifisan.nio.component.DefaultUDPEndPoint;
import com.gifisan.nio.component.NIOContext;
import com.gifisan.nio.component.UDPEndPoint;

public class UDPEndPointFactory {

	private Map<SocketAddress, DefaultUDPEndPoint>	endPoints	= new HashMap<SocketAddress, DefaultUDPEndPoint>();

	public UDPEndPoint getUDPEndPoint(NIOContext context, SelectionKey selectionKey, InetSocketAddress remote)
			throws SocketException {

		DefaultUDPEndPoint endPoint = endPoints.get(remote);

		if (endPoint == null) {
			endPoint = new DefaultUDPEndPoint(context, selectionKey, remote);
			selectionKey.attach(endPoint);
			endPoints.put(remote, endPoint);
		}

		return endPoint;
	}

	public void removeUDPEndPoint(UDPEndPoint endPoint) {
		endPoints.remove(endPoint.getRemoteSocketAddress());
	}
}
