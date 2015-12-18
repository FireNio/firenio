package com.gifisan.mtp.component;

import java.io.IOException;

class MTPChannelException extends IOException{
	
	protected MTPChannelException(String message){
		super(message);
	}
	
	protected MTPChannelException(String message,Throwable cause){
		super(message,cause);
	}
}
