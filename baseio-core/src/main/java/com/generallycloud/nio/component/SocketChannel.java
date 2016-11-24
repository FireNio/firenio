package com.generallycloud.nio.component;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.generallycloud.nio.component.SelectorLoop.SelectorLoopEvent;
import com.generallycloud.nio.protocol.ChannelReadFuture;
import com.generallycloud.nio.protocol.ChannelWriteFuture;
import com.generallycloud.nio.protocol.ProtocolDecoder;
import com.generallycloud.nio.protocol.ProtocolEncoder;
import com.generallycloud.nio.protocol.ProtocolFactory;
import com.generallycloud.nio.protocol.SslReadFuture;

public interface SocketChannel extends DuplexChannel, SelectorLoopEvent {

	public abstract void setWriteFuture(ChannelWriteFuture future);

	public abstract ChannelWriteFuture getWriteFuture();

	public abstract boolean isNetworkWeak();

	public abstract void upNetworkState();

	public abstract void downNetworkState();

	public abstract void wakeup();
	
	public abstract boolean isInSelectorLoop();

	public abstract ChannelReadFuture getReadFuture();

	public abstract SslReadFuture getSslReadFuture();

	public abstract void setReadFuture(ChannelReadFuture future);

	public abstract void setSslReadFuture(SslReadFuture future);

	public abstract int read(ByteBuffer buffer) throws IOException;

	public abstract int write(ByteBuffer buffer) throws IOException;

	public abstract void offer(ChannelWriteFuture future);

	public abstract boolean isBlocking();

	public abstract ProtocolEncoder getProtocolEncoder();

	public abstract void setProtocolEncoder(ProtocolEncoder protocolEncoder);

	public abstract ProtocolDecoder getProtocolDecoder();

	public abstract void setProtocolDecoder(ProtocolDecoder protocolDecoder);

	public abstract ProtocolFactory getProtocolFactory();

	public abstract void setProtocolFactory(ProtocolFactory protocolFactory);

	public abstract int getWriteFutureSize();

	public abstract boolean needFlush();
	
	public abstract UnsafeSession getSession();
	
	public abstract void fireEvent(SelectorLoopEvent event);
}
