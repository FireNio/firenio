package com.generallycloud.nio.protocol;

import java.nio.ByteBuffer;

import com.generallycloud.nio.protocol.ReadFuture;

public interface SslReadFuture extends ReadFuture{

	public ByteBuffer getMemory();
	
}
