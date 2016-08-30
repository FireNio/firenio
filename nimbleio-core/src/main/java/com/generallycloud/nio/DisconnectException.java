package com.generallycloud.nio;

import java.io.IOException;

public class DisconnectException extends IOException {
	
	public DisconnectException(String message, Exception cause) {
		super(message, cause);
	}

	public DisconnectException(String message) {
		super(message);
	}
}
