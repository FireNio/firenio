package com.gifisan.mtp.jms;

public interface Message {
	
	public static int TYPE_ERROR = 0;
	
	public static int TYPE_NULL = 1;
	
	public static int TYPE_TEXT = 2;
	
	public static int TYPE_BYTE = 3;
	
	public static int TYPE_STREAM = 4;

	public abstract String getQueueName();
	
	public abstract String getMessageID();
	
	public abstract int getMsgType();
	
	public abstract long createTime();
}
