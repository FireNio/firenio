package com.gifisan.nio.jms;

public class NullMessage extends MessageImpl implements Message{
	
	public static final NullMessage NULL_MESSAGE = new NullMessage();

	private NullMessage() {
		super(null, null);
	}

	public int getMsgType() {
		return Message.TYPE_NULL;
	}
}
