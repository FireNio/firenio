package com.gifisan.mtp.jms;

public class JMSException extends Exception{

	
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
