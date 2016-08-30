package com.generallycloud.nio.component.protocol;

import java.io.IOException;

import com.generallycloud.nio.component.TCPEndPoint;

public interface IOWriteFuture extends WriteFuture {

	public abstract boolean write() throws IOException;

	public abstract TCPEndPoint getEndPoint();

	public abstract void onException(IOException e);

	public abstract void onSuccess();
}
