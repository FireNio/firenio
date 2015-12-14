package com.gifisan.mtp.jms.client;

import com.alibaba.fastjson.JSONObject;
import com.gifisan.mtp.client.Response;
import com.gifisan.mtp.jms.ErrorMessage;
import com.gifisan.mtp.jms.JMSException;
import com.gifisan.mtp.jms.Message;
import com.gifisan.mtp.jms.TextMessage;

public class MessageParser {
	
	public static Message parse(Response response) throws JMSException{
		String content = response.getContent();
		return parse(content);
	}
	
	private static Message parse(String content){
		JSONObject object = JSONObject.parseObject(content);
		int msgType = object.getIntValue("msgType");
		Message message = messageParsesFromJSON[msgType].parse(object);
		
		return message;
	}
	
	static interface MessageParseFromJSON {
		
		Message parse(JSONObject object);
	}
	
	private static MessageParseFromJSON[] messageParsesFromJSON = new MessageParseFromJSON[]{
		//ERROR Message
		new MessageParseFromJSON() {
			
			public Message parse(JSONObject object) {
				ErrorMessage message = new ErrorMessage(object.getIntValue("code"));
				return message;
			}
		},
		//NULL Message
		new MessageParseFromJSON() {
			
			public Message parse(JSONObject object) {
				return null;
			}
		},
		//Text Message
		new MessageParseFromJSON() {
			
			public Message parse(JSONObject object) {
				String messageID = object.getString("messageID");
				String queueName = object.getString("queueName");
				String content = object.getString("content");
				TextMessage message = new TextMessage(messageID,queueName,content);
				
				
				return message;
			}
		},
		new MessageParseFromJSON() {
			
			public Message parse(JSONObject object) {
				return null;
			}
		},
		new MessageParseFromJSON() {
			
			public Message parse(JSONObject object) {
				return null;
			}
		}
		
		
	};
}
