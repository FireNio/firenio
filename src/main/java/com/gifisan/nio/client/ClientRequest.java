package com.gifisan.nio.client;

public class ClientRequest {

	private byte		sessionID		= 0;
	private String		serviceName	= null;
	private String		content		= null;

	public ClientRequest(byte sessionID, String serviceName, String content) {
		this.sessionID = sessionID;
		this.serviceName = serviceName;
		this.content = content;
	}

	protected byte getSessionID() {
		return sessionID;
	}

	protected String getServiceName() {
		return serviceName;
	}

	protected String getContent() {
		return content;
	}

}
