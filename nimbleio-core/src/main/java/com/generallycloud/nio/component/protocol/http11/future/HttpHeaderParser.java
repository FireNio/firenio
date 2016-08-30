package com.generallycloud.nio.component.protocol.http11.future;

public interface HttpHeaderParser {

	public static final String	CONTENT_TYPE_MULTIPART					= "multipart/form-data";
	public static final String	CONTENT_TYPE_TEXT_PLAIN				= "text/plain";
	public static final String	CONTENT_TYPE_TEXT_CSS					= "text/css";
	public static final String	CONTENT_TYPE_TEXT_HTML					= "text/html";
	public static final String	CONTENT_TYPE_IMAGE_PNG					= "image/png";
	public static final String	CONTENT_TYPE_IMAGE_GIF					= "image/gif";
	public static final String	CONTENT_TYPE_IMAGE_JPEG				= "image/jpeg";
	public static final String	CONTENT_TYPE_IMAGE_ICON				= "image/x-icon";
	public static final String	CONTENT_APPLICATION_URLENCODED			= "application/x-www-form-urlencoded";
	public static final String	CONTENT_APPLICATION_OCTET_STREAM		= "application/octet-stream";
	public static final String	CONTENT_APPLICATION_JAVASCRIPT			= "application/x-javascript";

	public abstract void parseHeader(String content, AbstractHttpReadFuture future);

}