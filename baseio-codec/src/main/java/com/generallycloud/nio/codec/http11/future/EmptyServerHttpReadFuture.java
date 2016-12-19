package com.generallycloud.nio.codec.http11.future;

import java.util.Map;

import com.generallycloud.nio.component.SocketChannelContext;

public class EmptyServerHttpReadFuture extends ServerHttpReadFuture{

	public EmptyServerHttpReadFuture(SocketChannelContext context) {
		super(context);
	}

	@Override
	protected void parseFirstLine(String line) {
		
	}

	@Override
	protected void parseContentType(String contentType) {
		
	}

	@Override
	protected void setDefaultResponseHeaders(Map<String, String> headers) {
	}
}
