package com.generallycloud.nio.protocol;

import java.io.IOException;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLException;

import com.generallycloud.nio.Linkable;
import com.generallycloud.nio.common.ssl.SslHandler;
import com.generallycloud.nio.component.IOSession;
import com.generallycloud.nio.component.SocketChannel;

public interface IOWriteFuture extends WriteFuture, Linkable<IOWriteFuture> {

	public abstract boolean write(SocketChannel channel) throws IOException;

	public IOWriteFuture duplicate();

	public abstract void onException(IOSession session, Exception e);

	public abstract void onSuccess(IOSession session);

	public abstract void wrapSSL(SSLEngine engine, SslHandler handler) throws SSLException, IOException;
}
