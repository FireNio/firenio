package com.gifisan.mtp.client;

import com.gifisan.mtp.component.InputStream;

public class Response {

	public static final int	STREAM		= 1;
	public static final int	TEXT			= 0;
	private String			content		= null;
	private InputStream		inputStream	= null;
	private byte			sessionID		= 0;
	private byte			type			= TEXT;

	public Response(InputStream inputStream) {
		this.inputStream = inputStream;
		this.type = STREAM;
	}

	public Response(String content, byte sessionID) {
		this.content = content;
		this.sessionID = sessionID;
	}

	public String getContent() {
		return content;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public byte getSessionID() {
		return sessionID;
	}

	public byte getType() {
		return type;
	}
}
