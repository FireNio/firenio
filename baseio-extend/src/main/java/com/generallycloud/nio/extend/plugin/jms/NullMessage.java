package com.generallycloud.nio.extend.plugin.jms;

import com.alibaba.fastjson.JSONObject;


public class NullMessage extends BasicMessage implements Message{
	
	public static final NullMessage NULL_MESSAGE = new NullMessage();

	private NullMessage() {
		super(null, null);
	}

	public int getMsgType() {
		return Message.TYPE_NULL;
	}
	
	public String toString() {
		return "{\"msgType\":1}";
	}
	
	public static void main(String[] args) {
		
		System.out.println(JSONObject.toJSON(NULL_MESSAGE).toString());
		System.out.println(NULL_MESSAGE.toString());
	}

}
