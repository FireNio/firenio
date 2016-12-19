package com.generallycloud.nio.component;

import java.io.IOException;
import java.net.SocketAddress;

import com.generallycloud.nio.protocol.DatagramPacket;

public interface DatagramChannel extends Channel {

	public abstract void sendPacket(DatagramPacket packet, SocketAddress socketAddress) throws IOException;

	public abstract void sendPacket(DatagramPacket packet) throws IOException;
	
	@Override
	public abstract UnsafeDatagramSession getSession();
	
	@Override
	public abstract DatagramChannelContext getContext();

}
