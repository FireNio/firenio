package com.gifisan.nio.client;

import java.io.InputStream;

public class ClientRequest {

	private byte			sessionID		= 0;
	private String			serviceName	= null;
	private String			text			= null;
	private InputStream		inputStream	= null;

	public byte getSessionID() {
		return sessionID;
	}

	public void setSessionID(byte sessionID) {
		this.sessionID = sessionID;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

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

}
