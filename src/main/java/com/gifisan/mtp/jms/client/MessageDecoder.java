package com.gifisan.mtp.jms.client;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.mtp.client.Response;
import com.gifisan.mtp.jms.ErrorMessage;
import com.gifisan.mtp.jms.JMSException;
import com.gifisan.mtp.jms.Message;
import com.gifisan.mtp.jms.TextMessage;

public class MessageDecoder {
	
	public static Message decode(Response response) throws JMSException{
		String content = response.getContent();
		return decode(content);
	}
	
	private static Message decode(String content){
		JSONObject object = JSONObject.parseObject(content);
		int msgType = object.getIntValue("msgType");
		Message message = messageParsesFromJSON[msgType].decode(object);
		
		return message;
	}
	
	static interface MessageDecodeFromJSON {
		
		Message decode(JSONObject object);
	}
	
	private static MessageDecodeFromJSON[] messageParsesFromJSON = new MessageDecodeFromJSON[]{
		//ERROR Message
		new MessageDecodeFromJSON() {
			
			public Message decode(JSONObject object) {
				ErrorMessage message = new ErrorMessage(object.getIntValue("code"));
				return message;
			}
		},
		//NULL Message
		new MessageDecodeFromJSON() {
			
			public Message decode(JSONObject object) {
				return null;
			}
		},
		//Text Message
		new MessageDecodeFromJSON() {
			
			public Message decode(JSONObject object) {
				String messageID = object.getString("messageID");
				String queueName = object.getString("queueName");
				String content = object.getString("content");
				TextMessage message = new TextMessage(messageID,queueName,content);
				
				
				return message;
			}
		},
		new MessageDecodeFromJSON() {
			
			public Message decode(JSONObject object) {
				return null;
			}
		},
		new MessageDecodeFromJSON() {
			
			public Message decode(JSONObject object) {
				return null;
			}
		}
		
		
	};
}
