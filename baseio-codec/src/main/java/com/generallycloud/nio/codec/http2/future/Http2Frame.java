package com.generallycloud.nio.codec.http2.future;

import com.generallycloud.nio.protocol.ChannelReadFuture;

public interface Http2Frame extends ChannelReadFuture{
	
	public abstract byte getFlags();

	public abstract Http2FrameType getHttp2FrameType();
	
	public abstract int getStreamIdentifier();
	
	public abstract void setStreamIdentifier(int streamIdentifier);
	
}
