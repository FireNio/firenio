package com.yoocent.mtp.jms.client;

public interface Message {
	
	public static int TYPE_TEXT = 0;
	
	public static int TYPE_BYTE = 1;
	
	public static int TYPE_STREAM = 2;

	public abstract String getQueueName();
	
	public abstract String getMessageID();
	
	public abstract int getMsgType();
	
	
}
