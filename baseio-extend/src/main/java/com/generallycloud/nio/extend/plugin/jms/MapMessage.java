package com.generallycloud.nio.extend.plugin.jms;

import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.generallycloud.nio.common.StringUtil;


public class MapMessage extends BasicMessage implements MappedMessage{
	
	private JSONObject map = null;
	
	public MapMessage(String messageID,String queueName) {
		super(messageID,queueName);
		this.map = new JSONObject();
	}
	
	public MapMessage(String messageID,String queueName,JSONObject map) {
		super(messageID,queueName);
		this.map = map;
	}
	
	public boolean getBooleanParameter(String key) {
		if (map == null) {
			return false;
		}
		return map.getBooleanValue(key);
	}
	
	public int getIntegerParameter(String key) {
		return getIntegerParameter(key, 0);
	}

	public int getIntegerParameter(String key, int defaultValue) {
		if (map == null) {
			return defaultValue;
		}
		int value = map.getIntValue(key);
		if (value == 0) {
			return defaultValue;
		}
		return value;
	}
	
	public String getEventName() {
		return getParameter("eventName");
	}

	public void setEventName(String eventName) {
		this.put("eventName", eventName);
	}

	public JSONArray getJSONArray(String key) {
		return map.getJSONArray(key);
	}
	
	public JSONObject getJSONObject(String key) {
		return map.getJSONObject(key);
	}

	public long getLongParameter(String key) {
		return getLongParameter(key, 0);
	}

	public long getLongParameter(String key, long defaultValue) {
		if (map == null) {
			return defaultValue;
		}
		long value = map.getLongValue(key);
		if (value == 0) {
			return defaultValue;
		}
		return value;
	}

	public int getMsgType() {
		return Message.TYPE_MAP;
	}

	public Object getObjectParameter(String key) {
		if (map == null) {
			return null;
		}
		return map.get(key);
	}

	public String getParameter(String key) {
		return getParameter(key, null);
	}

	public String getParameter(String key, String defaultValue) {
		if (map == null) {
			return defaultValue;
		}
		String value = map.getString(key);
		if (StringUtil.isNullOrBlank(value)) {
			return defaultValue;
		}
		return value;
	}

	protected String getText0(){
		return map.toJSONString();
	}

	public void put(Map value){
		this.map.putAll(value);
	}

	public void put(String key,Object value){
		this.map.put(key, value);
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
	
	public static void main(String[] args) {
		
		
		MapMessage message = new MapMessage("mid","qname");
		
		message.put("aaa","aaa1111");

		String str = message.toString();
		
		System.out.println(str);
		
		JSONObject.parseObject(str);
		
		System.out.println();
		
		
		
	}
}
