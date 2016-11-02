package com.generallycloud.nio.codec.http2.future;

public interface Http2FrameHeader extends Http2Frame{

	public abstract int getLength();

	public abstract int getType();

	public abstract byte getFlags();

	public abstract int getStreamIdentifier();
}
