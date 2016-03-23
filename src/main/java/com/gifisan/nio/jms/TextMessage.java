package com.gifisan.nio.jms;

import com.alibaba.fastjson.JSONObject;


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
	
	
	public String toString() {
		return new StringBuilder(24)
			.append("{\"msgType\":2,\"msgID\":\"")
			.append(getMsgID())
			.append("\",\"queueName\":\"")
			.append(getQueueName())
			.append("\",\"timestamp\":")
			.append(getTimestamp())
			.append(",\"content\":\"")
			.append(content)
			.append("\"}")
			.toString();
	}

	public static void main(String[] args) {
		
		
		
		TextMessage message = new TextMessage("mid","qname","wwwwwwwwwwwww");
		
		System.out.println(JSONObject.toJSON(message).toString());
		System.out.println(message.toString());
	}
}
