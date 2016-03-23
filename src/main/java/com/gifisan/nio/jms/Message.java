package com.gifisan.nio.jms;

public interface Message {

	public static int	TYPE_ERROR	= 0;

	public static int	TYPE_NULL	= 1;

	public static int	TYPE_TEXT	= 2;

	public static int	TYPE_BYTE	= 3;

	public abstract String getQueueName();

	public abstract String getMsgID();

	public abstract int getMsgType();

	public abstract long getTimestamp();
}
