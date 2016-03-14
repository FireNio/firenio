package com.gifisan.nio.jms;


public class TextMessage extends BasicMessage{
	
	private String content = null;

	public TextMessage(String messageID,String queueName,String content) {
		super(messageID,queueName);
		this.content = content;
	}

	public String getContent() {
		return content;
	}

	public int getMsgType() {
		return Message.TYPE_TEXT;
	}
	
}
