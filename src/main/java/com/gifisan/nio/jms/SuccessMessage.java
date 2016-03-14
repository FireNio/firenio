package com.gifisan.nio.jms;

public class SuccessMessage extends BasicMessage implements Message{
	
	public static final SuccessMessage NULL_MESSAGE = new SuccessMessage();

	private SuccessMessage() {
		super(null, null);
	}

	public int getMsgType() {
		return Message.TYPE_NULL;
	}
}
