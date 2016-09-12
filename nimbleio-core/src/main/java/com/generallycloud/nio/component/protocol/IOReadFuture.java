package com.generallycloud.nio.component.protocol;

import java.io.IOException;

import com.generallycloud.nio.component.TCPEndPoint;

public interface IOReadFuture extends ReadFuture{
	
	public abstract boolean read() throws IOException;

	public abstract void flush();
	
	public abstract boolean isBeatPacket();
	
	public abstract TCPEndPoint getTCPEndPoint();
	
}
