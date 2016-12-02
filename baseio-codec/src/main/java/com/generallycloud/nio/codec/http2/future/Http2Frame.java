package com.generallycloud.nio.codec.http2.future;

import com.generallycloud.nio.protocol.ReadFuture;

public interface Http2Frame extends ReadFuture{
	
	public abstract byte getFlags();

	public abstract Http2FrameType getHttp2FrameType();
	
	public abstract int getStreamIdentifier();
	
	public abstract void setStreamIdentifier(int streamIdentifier);
	
}
