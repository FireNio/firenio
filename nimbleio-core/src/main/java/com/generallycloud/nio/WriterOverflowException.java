package com.generallycloud.nio;

import java.io.IOException;

public class WriterOverflowException extends IOException {
	
	public static WriterOverflowException INSTANCE = new WriterOverflowException("writer overflow");

	public WriterOverflowException(String message, Exception cause) {
		super(message, cause);
	}

	public WriterOverflowException(String message) {
		super(message);
	}
}
