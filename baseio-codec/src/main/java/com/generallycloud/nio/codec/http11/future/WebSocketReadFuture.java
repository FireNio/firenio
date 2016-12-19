package com.generallycloud.nio.codec.http11.future;

import com.generallycloud.nio.protocol.NamedReadFuture;

public interface WebSocketReadFuture extends NamedReadFuture {

	public static final int		OP_CONTINUATION_FRAME		= 0;
	public static final int		OP_TEXT_FRAME				= 1;
	public static final int		OP_BINARY_FRAME			= 2;
	public static final int		OP_CONNECTION_CLOSE_FRAME	= 8;
	public static final int		OP_PING_FRAME				= 9;
	public static final int		OP_PONG_FRAME				= 10;

	public static final int		HEADER_LENGTH				= 2;

	public static final String	SESSION_KEY_SERVICE_NAME		= "_SESSION_KEY_SERVICE_NAME";

	public abstract boolean isEof();

	public abstract int getType();

	public abstract int getLength();
	
	public abstract boolean isCloseFrame();

	public abstract byte[] getByteArray();

}