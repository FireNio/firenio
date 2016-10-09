package com.generallycloud.nio.protocol;

public class ProtocolException extends RuntimeException{

	public ProtocolException(String message, Exception cause) {
		super(message, cause);
	}

	public ProtocolException(String message) {
		super(message);
	}
}
