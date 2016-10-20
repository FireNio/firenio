package com.generallycloud.nio.extend.plugin.rtp;

import java.io.IOException;

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
