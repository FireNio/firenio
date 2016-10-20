package com.generallycloud.nio.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.generallycloud.nio.component.IOSession;

public interface IOReadFuture extends ReadFuture {

	public abstract void flush();

//	public abstract SocketChannel getSocketChannel();
	
	public abstract boolean isHeartbeat();

	public abstract boolean isPING();

	public abstract boolean isPONG();

	public abstract boolean read(IOSession session,ByteBuffer buffer) throws IOException;

	public abstract IOReadFuture setPING();

	public abstract IOReadFuture setPONG();

//	public abstract void update(IOSession session);

}
