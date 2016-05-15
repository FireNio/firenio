package com.gifisan.nio.client;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.channels.SelectionKey;

import com.gifisan.nio.component.ClientUDPEndPoint;
import com.gifisan.nio.component.UDPEndPoint;
import com.gifisan.nio.component.UDPEndPointFactory;
import com.gifisan.nio.server.NIOContext;

public class ClientUDPEndPointFactory implements UDPEndPointFactory {
	
	private ClientUDPEndPoint clientUDPEndPoint = null;
	
	public ClientUDPEndPointFactory(ClientUDPEndPoint clientUDPEndPoint) {
		this.clientUDPEndPoint = clientUDPEndPoint;
	}

	public ClientUDPEndPoint getUDPEndPoint(NIOContext context, SelectionKey selectionKey, InetSocketAddress remote)
			throws SocketException {
		return clientUDPEndPoint;
	}

	public void removeUDPEndPoint(UDPEndPoint endPoint) {
		
	}

	
}
