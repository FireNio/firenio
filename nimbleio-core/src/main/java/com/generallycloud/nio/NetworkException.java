package com.generallycloud.nio;

import java.io.IOException;

public class NetworkException extends IOException {

	public NetworkException(String message, Exception cause) {
		super(message, cause);
	}

	public NetworkException(String message) {
		super(message);
	}
}
