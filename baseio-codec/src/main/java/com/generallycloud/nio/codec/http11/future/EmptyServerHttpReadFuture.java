package com.generallycloud.nio.codec.http11.future;

import java.util.Map;

import com.generallycloud.nio.component.SocketChannelContext;

public class EmptyServerHttpReadFuture extends ServerHttpReadFuture{

	public EmptyServerHttpReadFuture(SocketChannelContext context) {
		super(context);
	}

	protected void parseFirstLine(String line) {
		
	}

	protected void parseContentType(String contentType) {
		
	}

	protected void setDefaultResponseHeaders(Map<String, String> headers) {
	}
}
