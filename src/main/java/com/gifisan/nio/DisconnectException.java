package com.gifisan.nio;

import java.io.IOException;

public class DisconnectException extends IOException {
	
	public static DisconnectException INSTANCE = new DisconnectException("disconnected");

	public DisconnectException(String message, Exception cause) {
		super(message, cause);
	}

	public DisconnectException(String message) {
		super(message);
	}
}
