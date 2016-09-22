package com.generallycloud.nio.component.protocol;

import java.io.IOException;

import com.generallycloud.nio.component.IOSession;
import com.generallycloud.nio.component.SocketChannel;

public interface IOWriteFuture extends WriteFuture {

	public abstract boolean write() throws IOException;

	public abstract SocketChannel getSocketChannel();

	public IOWriteFuture duplicate(IOSession session);

	public abstract void onException(IOException e);

	public abstract void onSuccess();
}
