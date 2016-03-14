package com.gifisan.nio.jms;

import com.alibaba.fastjson.JSONObject;

public abstract class BasicMessage implements Message{
	
	private long createTime = System.currentTimeMillis();
	
	private String queueName;
	
	private String messageID;

	public String getMessageID() {
		return messageID;
	}

	public String getQueueName() {
		return queueName;
	}

	public BasicMessage(String messageID,String queueName) {
		this.messageID = messageID;
		this.queueName = queueName;
	}

	public long createTime() {
		return createTime;
	}



	private String json = null;
	
	public String toString(){
		if (json == null) {
			json = JSONObject.toJSONString(this);
		}
		return json;
	}
	
}
