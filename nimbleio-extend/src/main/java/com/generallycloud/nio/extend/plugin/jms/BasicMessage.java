package com.generallycloud.nio.extend.plugin.jms;

public abstract class BasicMessage implements Message {

	private long		timestamp	= System.currentTimeMillis();
	private String		queueName	;
	private String		msgID	;

	public String getMsgID() {
		return msgID;
	}

	public String getQueueName() {
		return queueName;
	}

	public BasicMessage(String msgID, String queueName) {
		this.msgID = msgID;
		this.queueName = queueName;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public abstract String toString();

	public Object getKey() {
		return getMsgID();
	}
}
