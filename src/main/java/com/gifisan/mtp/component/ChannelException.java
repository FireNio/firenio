package com.gifisan.mtp.component;

import java.io.IOException;

class ChannelException extends IOException{
	
	protected ChannelException(String message){
		super(message);
	}
	
	protected ChannelException(String message,Throwable cause){
		super(message,cause);
	}
}
