package com.yoocent.mtp.jms.server;

import java.util.HashMap;
import java.util.Map;

import com.yoocent.mtp.common.StringUtil;
import com.yoocent.mtp.jms.JMSException;
import com.yoocent.mtp.jms.client.Message;
import com.yoocent.mtp.server.Request;
import com.yoocent.mtp.server.session.Session;

public class JMSUtil {

	
	private static Map<String, MessageGroup> messageGroups = new HashMap<String, MessageGroup>();
	
	
	public static boolean reg(Message message){
		
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
		return group.offer(message);
		
	}
	
	
	
	public static Message pollMessage(Request request,long timeout) throws JMSException, InterruptedException{
		
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
		
		
		if(0 == timeout){
			Session session = request.getSession();
			
			Message message = group.poll(1024);
			while(session.connecting() && message == null){
				message = group.poll(1024);
			}
			return message;
		}
		
		
		Message message = group.poll(timeout);
		
		return message;
	}
	
	
	
}
