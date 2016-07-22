package com.gifisan.nio.component.protocol.http11.future;

public interface HttpHeaderParser {

	public abstract void parseHeader(String content, AbstractHttpReadFuture future);

}