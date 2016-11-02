package com.generallycloud.nio.codec.http2.future;

import com.generallycloud.nio.protocol.IOReadFuture;

public interface Http2Frame extends IOReadFuture{

	public Http2FrameType getHttp2FrameType();
	
}
