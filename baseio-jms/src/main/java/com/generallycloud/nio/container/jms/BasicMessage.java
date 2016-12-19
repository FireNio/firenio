package com.generallycloud.nio.container.jms;

public abstract class BasicMessage implements Message {

	private long		timestamp	= System.currentTimeMillis();
	private String		queueName	;
	private String		msgID	;

	@Override
	public String getMsgID() {
		return msgID;
	}

	@Override
	public String getQueueName() {
		return queueName;
	}

	public BasicMessage(String msgID, String queueName) {
		this.msgID = msgID;
		this.queueName = queueName;
	}

	@Override
	public long getTimestamp() {
		return timestamp;
	}

	@Override
	public abstract String toString();

	public Object getKey() {
		return getMsgID();
	}
}
