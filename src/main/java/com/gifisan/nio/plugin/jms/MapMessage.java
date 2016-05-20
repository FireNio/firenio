package com.gifisan.nio.plugin.jms;

import java.util.Map;

import com.alibaba.fastjson.JSONObject;


public class MapMessage extends BasicMessage{
	
	private JSONObject map = null;

	public MapMessage(String messageID,String queueName,JSONObject map) {
		super(messageID,queueName);
		this.map = map;
	}
	
	public MapMessage(String messageID,String queueName) {
		super(messageID,queueName);
		this.map = new JSONObject();
	}
	
	public void put(String key,Object value){
		this.map.put(key, value);
	}
	
	public void put(Map value){
		this.map.putAll(value);
	}
	
	public JSONObject getJSONObject(){
		return map;
	}

	public int getMsgType() {
		return Message.TYPE_MAP;
	}
	
	
	public String toString() {
		return new StringBuilder(24)
			.append("{\"msgType\":4,\"msgID\":\"")
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
	
	private String getText0(){
		return map.toJSONString();
	}

	public static void main(String[] args) {
		
		
		MapMessage message = new MapMessage("mid","qname");
		
		message.put("aaa","aaa1111");

		String str = message.toString();
		
		System.out.println(str);
		
		JSONObject.parseObject(str);
		
		System.out.println();
		
		
		
	}
}
