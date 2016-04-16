package com.gifisan.nio.jms;

import com.alibaba.fastjson.JSONObject;

public class ByteMessage extends TextMessage {

	private byte[]	array	= null;

	public ByteMessage(String messageID, String queueName,String text, byte[] array) {
		super(messageID, queueName,text);
		this.array = array;
	}

	public byte[] getByteArray() {
		return array;
	}
	
	public String toString() {
		return new StringBuilder(24)
			.append("{\"msgType\":3,\"msgID\":\"")
			.append(getMsgID())
			.append("\",\"queueName\":\"")
			.append(getQueueName())
			.append("\",\"timestamp\":")
			.append(getTimestamp())
			.append(",\"text\":\"")
			.append(getText0())
			.append("\"}")
			.toString();
	}

	public int getMsgType() {
		return Message.TYPE_BYTE;
	}
	
	public static void main(String[] args) {
		
		ByteMessage message = new ByteMessage("mid","qname","text",null);
		
		System.out.println(JSONObject.toJSON(message).toString());
		System.out.println(message.toString());
	}
}
