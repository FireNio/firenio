package com.gifisan.mtp.jms.server;

import java.util.HashMap;

import com.gifisan.mtp.AbstractLifeCycle;
import com.gifisan.mtp.common.LifeCycleUtil;
import com.gifisan.mtp.jms.Message;
import com.gifisan.mtp.jms.TextMessage;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;
import com.gifisan.mtp.server.session.Session;

public class MQContextImpl extends AbstractLifeCycle implements MQContext{
	
	private final int LOGINED = 1;
	
	private long dueTime = 0;

	private HashMap<String, Message> messageIDs = new HashMap<String, Message>();
	
	MQContextImpl(){}
	
	private P2PProductLine productLine = new P2PProductLine(this); 
	
	public boolean offerMessage(Message message){
		
		synchronized (messageIDs) {
			messageIDs.put(message.getMessageID(), message);
		}
		
		return productLine.offerMessage(message);
	}
	
	public void pollMessage(Request request,Response response) {
		
		productLine.pollMessage(request,response);
		
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
				String messageID = request.getParameter("messageID");
				String queueName = request.getParameter("queueName");
				String content = request.getParameter("content");
					TextMessage message = new TextMessage(messageID,queueName,content);
					
					
					return message;
			}
		}
		
	};

	public Message browser(String messageID) {
		
		return messageIDs.get(messageID);
	}

	protected void doStart() throws Exception {
		
		productLine.start();
		
		Thread lineThread = new Thread(productLine, "Message-product-line");
		
		lineThread.start();
		
	}
	
	public long getMessageDueTime() {
		return this.dueTime;
	}
	
	public void setMessageDueTime(long dueTime) {
		this.dueTime = dueTime;
	}

	protected void doStop() throws Exception {
		LifeCycleUtil.stop(productLine);
		
	}

	@Override
	public int messageSize() {
		// TODO 。。。。
		return 0;
	}
	
	
	
}
