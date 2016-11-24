package com.generallycloud.nio.component;

import java.io.IOException;
import java.net.SocketAddress;

import com.generallycloud.nio.buffer.ByteBuf;

public interface DatagramChannel extends Channel {

	public abstract void sendPacket(ByteBuf buf, SocketAddress socketAddress) throws IOException;

	public abstract void sendPacket(ByteBuf buf) throws IOException;
	
	public abstract UnsafeDatagramSession getSession();

}
