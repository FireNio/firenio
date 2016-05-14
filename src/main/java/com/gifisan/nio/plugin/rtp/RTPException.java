package com.gifisan.nio.plugin.rtp;

public class RTPException extends Exception{
	
	public RTPException(String reason){
		super(reason);
	}
	
	public RTPException(String reason,Throwable e){
		super(reason, e);
	}
	
	public RTPException(Throwable e){
		super(e);
	}
	
}
