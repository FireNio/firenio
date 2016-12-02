package com.generallycloud.nio.codec.http2.future;

import com.generallycloud.nio.protocol.ReadFuture;

public interface Http2FrameHeader extends ReadFuture {

	public abstract int getLength();

	public abstract int getType();

	public abstract byte getFlags();

	public abstract int getStreamIdentifier();
}
