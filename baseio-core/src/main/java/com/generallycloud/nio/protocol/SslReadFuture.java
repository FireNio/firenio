package com.generallycloud.nio.protocol;

import java.nio.ByteBuffer;

public interface SslReadFuture extends IOReadFuture{

	public ByteBuffer getMemory();
	
}
