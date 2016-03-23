package com.gifisan.nio.jms;

import com.alibaba.fastjson.JSONObject;

public class ByteMessage extends BasicMessage {

	private byte[]	content	= null;

	public ByteMessage(String messageID, String queueName, byte[] content) {
		super(messageID, queueName);
		this.content = content;
	}

	public byte[] getContent() {
		return content;
	}

	public int getMsgType() {
		return Message.TYPE_BYTE;
	}

	
	public String toString() {
		return new StringBuilder(24)
			.append("{\"msgType\":3,\"msgID\":\"")
			.append(getMsgID())
			.append("\",\"queueName\":\"")
			.append(getQueueName())
			.append("\",\"timestamp\":")
			.append(getTimestamp())
			.append("}")
			.toString();
	}

	public static void main(String[] args) {
		
		ByteMessage message = new ByteMessage("mid","qname",null);
		
		System.out.println(JSONObject.toJSON(message).toString());
		System.out.println(message.toString());
	}
}
