package com.gifisan.nio.component;

import java.io.IOException;

class NIOException extends IOException{
	
	protected NIOException(String message){
		super(message);
	}
	
	protected NIOException(String message,Throwable cause){
		super(message,cause);
	}
}
