package com.gifisan.nio.component.protocol.http11.future;

public interface HttpHeaderParser {
	
	public static final String	CONTENT_TYPE_URLENCODED	= "application/x-www-form-urlencoded";
	public static final String	CONTENT_TYPE_MULTIPART		= "multipart/form-data";
	public static final String	CONTENT_TYPE_TEXTPLAIN		= "text/plain";
	public static final String	CONTENT_OCTET_STREAM		= "application/octet-stream";

	public abstract void parseHeader(String content, AbstractHttpReadFuture future);

}