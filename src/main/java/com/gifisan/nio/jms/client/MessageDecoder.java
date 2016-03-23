package com.gifisan.nio.jms.client;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.nio.client.Response;
import com.gifisan.nio.jms.ByteMessage;
import com.gifisan.nio.jms.ErrorMessage;
import com.gifisan.nio.jms.JMSException;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.jms.TextMessage;

public class MessageDecoder {
	
	public static Message decode(Response response) throws JMSException{
		byte type = response.getProtocolType();
		
		
		
		String text = response.getText();
		return decode(text);
	}
	
	public static Message decode(String content){
		return decode(JSONObject.parseObject(content));
	}
	
	public static Message decode(JSONObject object){
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
				String messageID = object.getString("messageID");
				String queueName = object.getString("queueName");
				byte[] content = object.getBytes("content");
				ByteMessage message = new ByteMessage(messageID,queueName,content);
				
				return message;
			}
		}
		
		
	};
}
