package com.gifisan.nio.component;

import java.io.IOException;

import com.gifisan.nio.component.protocol.udp.DatagramPacket;

public interface DatagramPacketAcceptor {
	
	public void accept(UDPEndPoint endPoint,DatagramPacket packet) throws IOException;
}
