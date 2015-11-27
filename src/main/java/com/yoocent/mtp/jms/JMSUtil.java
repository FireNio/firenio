package com.yoocent.mtp.jms;

import java.util.HashMap;
import java.util.Map;

import com.yoocent.mtp.common.StringUtil;
import com.yoocent.mtp.server.Request;

public class JMSUtil {

	
	private static Map<String, MessageGroup> messageGroups = new HashMap<String, MessageGroup>();
	
	
	public static boolean reg(JMSMessage message){
		
		String serviceName = message.getServiceName();
		
		MessageGroup group = messageGroups.get(serviceName);
		
		if (group == null) {
			synchronized (messageGroups) {
				group = messageGroups.get(serviceName);
				if (group == null) {
					group = new MessageGroup();
					messageGroups.put(serviceName, group);
				}
			}
		}
		return group.offer(message);
		
	}
	
	
	
	public static JMSMessage pollMessage(Request request,long timeout) throws JMSException, InterruptedException{
		
		String serviceName = request.getStringParameter("service-name");
		
		if (StringUtil.isBlankOrNull(serviceName)) {
			throw new JMSException("null service name");
		}
		
		MessageGroup group = messageGroups.get(serviceName);
		
		if (group == null) {
			synchronized (messageGroups) {
				group = messageGroups.get(serviceName);
				if (group == null) {
					group = new MessageGroup();
					messageGroups.put(serviceName, group);
				}
			}
		}
		
		
		if(0 == timeout){
			JMSMessage message = group.poll(16);
			while(message == null){
				message = group.poll(16);
			}
			return message;
		}
		
		
		JMSMessage message = group.poll(timeout);
		
		return message;
	}
	
	
	
}
