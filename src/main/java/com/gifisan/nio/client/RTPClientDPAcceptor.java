package com.gifisan.nio.client;

import java.io.IOException;

import com.gifisan.nio.component.DatagramPacketAcceptor;
import com.gifisan.nio.component.UDPEndPoint;
import com.gifisan.nio.component.protocol.DatagramPacket;

public class RTPClientDPAcceptor implements DatagramPacketAcceptor{

	public void accept(UDPEndPoint endPoint, DatagramPacket packet) throws IOException {
		ClientSession session = (ClientSession) endPoint.getTCPSession();
		
		DatagramPacketAcceptor acceptor = session.getDatagramPacketAcceptor();
		
		acceptor.accept(endPoint, packet);
	}
	
}
