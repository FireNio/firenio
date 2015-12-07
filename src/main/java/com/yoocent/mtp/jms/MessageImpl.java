package com.yoocent.mtp.jms;

import com.alibaba.fastjson.JSONObject;

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

	private String json = null;
	
	public String toString(){
		if (json == null) {
			json = JSONObject.toJSONString(this);
		}
		return json;
	}
	
}
