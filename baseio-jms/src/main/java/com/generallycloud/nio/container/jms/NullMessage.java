package com.generallycloud.nio.container.jms;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;


public class NullMessage extends BasicMessage implements Message{
	
	public static final NullMessage NULL_MESSAGE = new NullMessage();

	private NullMessage() {
		super(null, null);
	}

	@Override
	public int getMsgType() {
		return Message.TYPE_NULL;
	}
	
	@Override
	public String toString() {
		return "{\"msgType\":1}";
	}
	
	public static void main(String[] args) {
		
		System.out.println(JSON.toJSON(NULL_MESSAGE).toString());
		System.out.println(NULL_MESSAGE.toString());
	}

}
