package com.generallycloud.nio;

@SuppressWarnings("serial")
public class PropertiesException extends Exception{

	
	public PropertiesException() {
	}

	public PropertiesException(String message) {
		super(message);
	}

	public PropertiesException(String message, Throwable cause) {
		super(message, cause);
	}
}
