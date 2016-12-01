package com.generallycloud.nio.container.rtp;

import java.io.IOException;

@SuppressWarnings("serial")
public class RTPException extends IOException{
	
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
