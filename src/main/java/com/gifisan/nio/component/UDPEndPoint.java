package com.gifisan.nio.component;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.gifisan.nio.component.protocol.udp.DatagramPacket;

public interface UDPEndPoint extends EndPoint{

	public abstract DatagramPacket readPacket(ByteBuffer buffer) throws IOException;
	
	public abstract Session getTCPSession();
	
	public abstract void setTCPSession(Session session);
	
}
