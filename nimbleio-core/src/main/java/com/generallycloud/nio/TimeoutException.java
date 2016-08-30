package com.generallycloud.nio;

import java.io.IOException;

public class TimeoutException extends IOException {
	
	public TimeoutException(String message, Exception cause) {
		super(message, cause);
	}

	public TimeoutException(String message) {
		super(message);
	}
}
