package com.generallycloud.nio.extend.plugin.jms;

import java.io.IOException;

public class MQException extends IOException{
	
	public static final MQException TIME_OUT = new MQException("timeout");
	
	public MQException(String reason){
		super(reason);
	}
	
	public MQException(String reason,Throwable e){
		super(reason, e);
	}
	
	public MQException(Throwable e){
		super(e);
	}
	
}
