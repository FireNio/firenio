package com.generallycloud.nio.acceptor;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.Map;

import com.generallycloud.nio.component.NioDatagramChannel;
import com.generallycloud.nio.component.NIOContext;
import com.generallycloud.nio.component.DatagramChannel;

public class UDPEndPointFactory {

	private Map<SocketAddress, NioDatagramChannel>	endPoints	= new HashMap<SocketAddress, NioDatagramChannel>();

	public DatagramChannel getUDPEndPoint(NIOContext context, SelectionKey selectionKey, InetSocketAddress remote)
			throws SocketException {

		NioDatagramChannel endPoint = endPoints.get(remote);

		if (endPoint == null) {
			endPoint = new NioDatagramChannel(context, selectionKey, remote);
			selectionKey.attach(endPoint);
			endPoints.put(remote, endPoint);
		}

		return endPoint;
	}

	public void removeUDPEndPoint(DatagramChannel endPoint) {
		endPoints.remove(endPoint.getRemoteSocketAddress());
	}
}
