package com.yoocent.mtp.jms;


public class TextMessage extends MessageImpl{
	
	public TextMessage(String messageID,String queueName,String content) {
		super(messageID,queueName);
		this.content = content;
	}

	public int getMsgType() {
		return Message.TYPE_TEXT;
	}

	private String content = null;

	public String getContent() {
		return content;
	}
	
}
