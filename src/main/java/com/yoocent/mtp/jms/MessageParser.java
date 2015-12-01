package com.yoocent.mtp.jms;

import com.alibaba.fastjson.JSONObject;
import com.yoocent.mtp.jms.client.Message;
import com.yoocent.mtp.jms.client.impl.TextMessage;
import com.yoocent.mtp.server.Request;

public class MessageParser {

	interface MessageParseFromRequest {
		
		Message parse(Request request);
	}
	
	
	
	public static Message parse(Request request){
		int msgType = request.getIntegerParameter("msgType");
		Message message = messageParsesFromRequest[msgType].parse(request);
		return message;
	}
	
	public static Message parse(String content){
		JSONObject object = JSONObject.parseObject(content);
		int msgType = object.getIntValue("msgType");
		Message message = messageParsesFromJSON[msgType].parse(object);
		
		return message;
	}
	
	interface MessageParseFromJSON {
		
		Message parse(JSONObject object);
	}
	
	private static MessageParseFromJSON[] messageParsesFromJSON = new MessageParseFromJSON[]{
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
	
	private static MessageParseFromRequest[] messageParsesFromRequest = new MessageParseFromRequest[]{
		new MessageParseFromRequest() {
			
			public Message parse(Request request) {
				String messageID = request.getStringParameter("messageID");
				String queueName = request.getStringParameter("queueName");
				String content = request.getStringParameter("content");
				TextMessage message = new TextMessage(messageID,queueName,content);
				
				
				return message;
			}
		}
		
		
	};
	
	
	
	
}
