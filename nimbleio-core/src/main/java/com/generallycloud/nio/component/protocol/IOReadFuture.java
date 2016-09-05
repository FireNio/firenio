package com.generallycloud.nio.component.protocol;

import java.io.IOException;
import java.io.InputStream;

import com.generallycloud.nio.component.TCPEndPoint;

public interface IOReadFuture extends ReadFuture{
	
	public abstract InputStream getInputStream();
	
	public abstract void setHasOutputStream(boolean hasOutputStream);

	public abstract boolean read() throws IOException;

	public abstract void flush();
	
	public abstract boolean isBeatPacket();
	
	public abstract TCPEndPoint getTCPEndPoint();
	
}
