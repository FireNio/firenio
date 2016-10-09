package com.generallycloud.nio.component;

import java.io.IOException;

import com.generallycloud.nio.protocol.IOWriteFuture;

public interface IOSession extends Session{
	
	public abstract void fireOpend();

	public abstract SocketChannel getSocketChannel();
	
	public abstract void flush(IOWriteFuture future) throws IOException;
}
