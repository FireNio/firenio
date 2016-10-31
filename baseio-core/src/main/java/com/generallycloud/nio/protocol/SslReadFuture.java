package com.generallycloud.nio.protocol;

import java.nio.ByteBuffer;

public interface SslReadFuture extends IOReadFuture{

	public static final int	SSL_CONTENT_TYPE_ALERT					= 21;

	public static final int	SSL_CONTENT_TYPE_APPLICATION_DATA		= 23;

	public static final int	SSL_CONTENT_TYPE_CHANGE_CIPHER_SPEC		= 20;

	public static final int	SSL_CONTENT_TYPE_HANDSHAKE				= 22;

	public static final int	SSL_RECORD_HEADER_LENGTH				= 5;

	public ByteBuffer getMemory();
	
}
