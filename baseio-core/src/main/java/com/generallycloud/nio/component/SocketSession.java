package com.generallycloud.nio.component;

import javax.net.ssl.SSLEngine;

import com.generallycloud.nio.component.concurrent.EventLoop;
import com.generallycloud.nio.component.ssl.SslHandler;
import com.generallycloud.nio.protocol.ChannelWriteFuture;
import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;
import com.generallycloud.nio.protocol.ReadFuture;

public interface SocketSession extends Session {

	public abstract boolean isEnableSSL();

	public abstract SSLEngine getSSLEngine();

	public abstract SslHandler getSslHandler();

	public abstract void finishHandshake(Exception e);

	public abstract ProtocolDecoder getProtocolDecoder();

	public abstract ProtocolEncoder getProtocolEncoder();

	public abstract ProtocolFactory getProtocolFactory();
	
	public abstract void setAttachment(int index, Object attachment);
	
	public abstract Object getAttachment(int index);
	
	public abstract SocketChannelContext getContext();
	
	public abstract String getProtocolID();

	public abstract boolean isBlocking();

	public abstract EventLoop getEventLoop();
	
	public abstract void flush(ReadFuture future) ;
	
	public abstract void flush(ChannelWriteFuture future);

	public abstract void setProtocolDecoder(ProtocolDecoder protocolDecoder);

	public abstract void setProtocolEncoder(ProtocolEncoder protocolEncoder);

	public abstract void setProtocolFactory(ProtocolFactory protocolFactory);

}
