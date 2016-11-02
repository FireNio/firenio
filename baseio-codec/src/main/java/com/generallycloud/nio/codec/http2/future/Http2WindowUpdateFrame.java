package com.generallycloud.nio.codec.http2.future;


public interface Http2WindowUpdateFrame extends Http2Frame {

	public abstract int getUpdateValue();
	
}
