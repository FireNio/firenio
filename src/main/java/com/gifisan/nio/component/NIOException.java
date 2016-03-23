package com.gifisan.nio.component;

import java.io.IOException;

public class NIOException extends IOException{
	
	public NIOException(String message){
		super(message);
	}
	
	public NIOException(String message,Throwable cause){
		super(message,cause);
	}
}
