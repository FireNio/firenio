package com.generallycloud.nio.component;

import java.io.IOException;
import java.net.SocketAddress;

import com.generallycloud.nio.protocol.DatagramPacket;

public interface DatagramSession extends Session {

	@Override
	public abstract DatagramChannelContext getContext();
	
	public abstract void sendPacket(DatagramPacket packet, SocketAddress socketAddress) throws IOException;

	public abstract void sendPacket(DatagramPacket packet) throws IOException;
	
}
