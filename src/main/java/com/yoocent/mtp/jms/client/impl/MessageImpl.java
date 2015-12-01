package com.yoocent.mtp.jms.client.impl;

import com.alibaba.fastjson.JSONObject;
import com.yoocent.mtp.jms.client.Message;

public abstract class MessageImpl implements Message{
	
	private String queueName;
	
	private String messageID;

	public String getMessageID() {
		return messageID;
	}

	public String getQueueName() {
		return queueName;
	}

	public MessageImpl(String messageID,String queueName) {
		this.messageID = messageID;
		this.queueName = queueName;
	}

	public String toString(){
		return JSONObject.toJSONString(this);
	}
	
}
