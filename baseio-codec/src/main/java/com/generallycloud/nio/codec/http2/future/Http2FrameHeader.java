package com.generallycloud.nio.codec.http2.future;

import com.generallycloud.nio.protocol.ChannelReadFuture;

public interface Http2FrameHeader extends ChannelReadFuture {

	public abstract int getLength();

	public abstract int getType();

	public abstract byte getFlags();

	public abstract int getStreamIdentifier();
}
