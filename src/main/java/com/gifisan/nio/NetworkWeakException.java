package com.gifisan.nio;

import java.io.IOException;

public class NetworkWeakException extends IOException {

	public NetworkWeakException(String message, Exception cause) {
		super(message, cause);
	}

	public NetworkWeakException(String message) {
		super(message);
	}
}
