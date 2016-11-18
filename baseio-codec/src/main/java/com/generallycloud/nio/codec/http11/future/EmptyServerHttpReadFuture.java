package com.generallycloud.nio.codec.http11.future;

import java.util.Map;

import com.generallycloud.nio.component.BaseContext;

public class EmptyServerHttpReadFuture extends ServerHttpReadFuture{

	public EmptyServerHttpReadFuture(BaseContext context) {
		super(context);
	}

	protected void parseFirstLine(String line) {
		
	}

	protected void parseContentType(String contentType) {
		
	}

	protected void setDefaultResponseHeaders(Map<String, String> headers) {
	}
}
