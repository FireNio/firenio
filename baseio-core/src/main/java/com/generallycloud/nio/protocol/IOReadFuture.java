package com.generallycloud.nio.protocol;

import java.io.IOException;

import com.generallycloud.nio.buffer.ByteBuf;
import com.generallycloud.nio.component.SocketSession;

public interface IOReadFuture extends ReadFuture {

	public abstract void flush();

	// public abstract SocketChannel getSocketChannel();

	public abstract boolean isHeartbeat();

	public abstract boolean isPING();

	public abstract boolean isPONG();

	public abstract boolean read(SocketSession session, ByteBuf buf) throws IOException;

	public abstract IOReadFuture setPING();

	public abstract IOReadFuture setPONG();

	public abstract boolean isSilent();

	public abstract void setSilent(boolean isSilent);

}
