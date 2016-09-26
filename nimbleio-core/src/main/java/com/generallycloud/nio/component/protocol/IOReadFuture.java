package com.generallycloud.nio.component.protocol;

import java.io.IOException;

import com.generallycloud.nio.component.SocketChannel;

public interface IOReadFuture extends ReadFuture {

	public abstract boolean read() throws IOException;

	public abstract void flush();

	public abstract boolean isHeartbeat();

	public abstract boolean isPING();

	public abstract boolean isPONG();

	public abstract IOReadFuture setPING();

	public abstract IOReadFuture setPONG();

	public abstract SocketChannel getSocketChannel();

}
