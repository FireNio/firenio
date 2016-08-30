package com.generallycloud.nio.extend.plugin.jms;


//增加消息时效
public interface Message{

	public static final int	TYPE_ERROR		= 0;
	public static final int	TYPE_NULL		= 1;
	public static final int	TYPE_TEXT		= 2;
	public static final int	TYPE_TEXT_BYTE	= 3;
	public static final int	TYPE_MAP			= 4;
	public static final int	TYPE_MAP_BYTE		= 5;

	public abstract String getQueueName();

	public abstract String getMsgID();

	public abstract int getMsgType();

	public abstract long getTimestamp();
}
