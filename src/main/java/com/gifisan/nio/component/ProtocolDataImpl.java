package com.gifisan.nio.component;

public class ProtocolDataImpl implements ProtocolData {

	private String			text			= null;
	private InputStream		inputStream	= null;
	private byte			protocolType	= 0;
	private byte			sessionID		= 0;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	public byte getProtocolType() {
		return protocolType;
	}

	public void setProtocolType(byte protocolType) {
		this.protocolType = protocolType;
	}

	public byte getSessionID() {
		return sessionID;
	}

	public void setSessionID(byte sessionID) {
		this.sessionID = sessionID;
	}

}
