package com.generallycloud.nio.codec.http2.future;

public interface Http2HeadersFrame extends Http2Frame {

	public static final int	FLAG_END_STREAM	= 0x1;
	public static final int	FLAG_END_HEADERS	= 0x4;
	public static final int	FLAG_PADDED		= 0x8;
	public static final int	FLAG_PRIORITY		= 0x20;

	public abstract boolean isE();

	public abstract int getStreamDependency();

	public abstract int getWeight();

	public abstract byte[] getFragment();
}
