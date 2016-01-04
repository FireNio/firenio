package com.gifisan.mtp.client;

import java.io.IOException;

class MTPChannelException extends IOException{
	
	protected MTPChannelException(String message){
		super(message);
	}
	
	protected MTPChannelException(String message,Throwable cause){
		super(message,cause);
	}
}
