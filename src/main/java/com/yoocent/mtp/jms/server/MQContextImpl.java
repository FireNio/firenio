package com.yoocent.mtp.jms.server;

import java.util.HashMap;
import java.util.Map;

import com.yoocent.mtp.common.StringUtil;
import com.yoocent.mtp.jms.JMSException;
import com.yoocent.mtp.jms.Message;
import com.yoocent.mtp.jms.TextMessage;
import com.yoocent.mtp.server.Request;
import com.yoocent.mtp.server.session.Session;

public class MQContextImpl implements MQContext{
	
	private final int LOGINED = 1;

	private Map<String, MessageGroup> messageGroups = new HashMap<String, MessageGroup>();
	
	private HashMap<String, Message> messageIDs = new HashMap<String, Message>();
	
	MQContextImpl(){}
	
	public boolean regist(Message message){
		
		String queueName = message.getQueueName();
		
		MessageGroup group = messageGroups.get(queueName);
		
		if (group == null) {
			synchronized (messageGroups) {
				group = messageGroups.get(queueName);
				if (group == null) {
					group = new MessageGroup();
					messageGroups.put(queueName, group);
				}
			}
		}
		
		synchronized (messageIDs) {
			messageIDs.put(message.getMessageID(), message);
		}
		
		return group.offer(message);
	}
	
	public Message pollMessage(Request request,long timeout) throws JMSException{
		
		String queueName = request.getStringParameter("queueName");
		
		if (StringUtil.isBlankOrNull(queueName)) {
			throw new JMSException("null queue name");
		}
		
		MessageGroup group = messageGroups.get(queueName);
		
		if (group == null) {
			synchronized (messageGroups) {
				group = messageGroups.get(queueName);
				if (group == null) {
					group = new MessageGroup();
					messageGroups.put(queueName, group);
				}
			}
		}
		
		
		Message message;
		try {
			if(0 == timeout){
				Session session = request.getSession();
				
				message = group.poll(1024);
				while(session.connecting() && message == null){
					message = group.poll(1024);
				}
				return message;
			}
			
			
			message = group.poll(timeout);
		} catch (InterruptedException e) {
			throw new JMSException(e.getMessage(),e);
		}
		
		synchronized (messageIDs) {
			messageIDs.remove(message.getMessageID());
		}
		
		return message;
	}
	


	public boolean isLogined(Session session) {
		return LOGINED == session.getComment();
	}

	public void setLogined(boolean logined,Session session) {
		session.setComment(LOGINED);
	}

	
	private interface MessageParseFromRequest {
		
		Message parse(Request request);
	}
	
	
	
	public Message parse(Request request){
		int msgType = request.getIntegerParameter("msgType");
		Message message = messageParsesFromRequest[msgType].parse(request);
		return message;
	}
	
	
	private MessageParseFromRequest[] messageParsesFromRequest = new MessageParseFromRequest[]{
		//ERROR Message
		null,
		//NULL Message
		null,
		//Text Message
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

	public Message browser(String messageID) {
		
		return messageIDs.get(messageID);
	}
	
	
}
