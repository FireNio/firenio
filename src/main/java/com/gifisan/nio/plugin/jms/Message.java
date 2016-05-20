package com.gifisan.nio.plugin.jms;

public interface Message {

	public static final int	TYPE_ERROR	= 0;
	public static final int	TYPE_NULL	= 1;
	public static final int	TYPE_TEXT	= 2;
	public static final int	TYPE_BYTE	= 3;
	public static final int	TYPE_MAP		= 4;

	public abstract String getQueueName();

	public abstract String getMsgID();

	public abstract int getMsgType();

	public abstract long getTimestamp();
}
