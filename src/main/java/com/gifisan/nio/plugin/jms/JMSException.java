package com.gifisan.nio.plugin.jms;

public class JMSException extends Exception{
	
	public static final JMSException TIME_OUT = new JMSException("timeout");
	
	public JMSException(String reason){
		super(reason);
	}
	
	public JMSException(String reason,Throwable e){
		super(reason, e);
	}
	
	public JMSException(Throwable e){
		super(e);
	}
	
}
