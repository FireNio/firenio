package com.generallycloud.nio.component.protocol.http11.future;

import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.component.protocol.NamedReadFuture;

public interface WebSocketReadFuture extends NamedReadFuture {

	public static final int	HEADER_LENGTH				= 2;

	public static final String	SESSION_KEY_SERVICE_NAME	= "_SESSION_KEY_SERVICE_NAME";

	public abstract boolean isEof();

	public abstract int getType();
	
	public abstract int getLength();

	public abstract  BufferedOutputStream getData();

}