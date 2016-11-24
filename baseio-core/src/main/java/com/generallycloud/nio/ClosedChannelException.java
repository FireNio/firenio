package com.generallycloud.nio;

import java.io.IOException;

@SuppressWarnings("serial")
public class ClosedChannelException extends IOException{
	
	public ClosedChannelException(String message, Exception cause) {
		super(message, cause);
	}

	public ClosedChannelException(String message) {
		super(message);
	}
	
}
