package com.generallycloud.nio.extend.plugin.jms;

import com.alibaba.fastjson.JSONObject;

public class MapByteMessage extends MapMessage implements MappedMessage ,BytedMessage{

	private byte[]	array	;

	public MapByteMessage(String messageID, String queueName,JSONObject map, byte[] array) {
		super(messageID, queueName,map);
		this.array = array;
	}
	
	public MapByteMessage(String messageID, String queueName,byte[] array) {
		super(messageID, queueName);
		this.array = array;
	}

	public byte[] getByteArray() {
		return array;
	}
	
	public String toString() {
		return new StringBuilder(24)
			.append("{\"msgType\":5,\"msgID\":\"")
			.append(getMsgID())
			.append("\",\"queueName\":\"")
			.append(getQueueName())
			.append("\",\"timestamp\":")
			.append(getTimestamp())
			.append(",\"map\":")
			.append(getText0())
			.append("}")
			.toString();
	}

	public int getMsgType() {
		return Message.TYPE_MAP_BYTE;
	}
	
	public static void main(String[] args) {
		
		MapByteMessage message = new MapByteMessage("mid","qname",null);
		
		message.put("aaa","aaa1111");

		String str = message.toString();
		
		System.out.println(str);
		
		JSONObject.parseObject(str);
		
		System.out.println();
	}
}
