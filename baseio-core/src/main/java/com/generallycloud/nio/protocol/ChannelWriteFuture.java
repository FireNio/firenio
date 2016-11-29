package com.generallycloud.nio.protocol;

import java.io.IOException;

import javax.net.ssl.SSLException;

import com.generallycloud.nio.Linkable;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.component.ssl.SslHandler;
import com.generallycloud.nio.component.SocketChannel;

public interface ChannelWriteFuture extends WriteFuture, Linkable<ChannelWriteFuture> {

	public abstract boolean write(SocketChannel channel) throws IOException;

	public abstract ChannelWriteFuture duplicate();
	
	public abstract ChannelWriteFuture duplicate(ReadFuture future);

	public abstract void onException(SocketSession session, Exception e);

	public abstract void onSuccess(SocketSession session);
	
	public abstract int getBinaryLength();

	public abstract void wrapSSL(SocketSession session, SslHandler handler) throws SSLException, IOException;
}
