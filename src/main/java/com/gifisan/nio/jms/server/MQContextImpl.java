package com.gifisan.nio.jms.server;

import java.util.HashMap;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.jms.ByteMessage;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.jms.TextMessage;
import com.gifisan.nio.server.Request;
import com.gifisan.nio.server.Response;
import com.gifisan.nio.server.session.Session;

public class MQContextImpl extends AbstractLifeCycle implements MQContext{

	private interface MessageParseFromRequest {

		Message parse(Request request);
	}

	private long					dueTime				= 0;
	private final int				LOGINED				= 1;
	private HashMap<String, Message>	messageIDs			= new HashMap<String, Message>();
	private P2PProductLine			p2pProductLine			= new P2PProductLine(this);
	private SubscribeProductLine		subProductLine			= new SubscribeProductLine(this);

	private MessageParseFromRequest[]	messageParsesFromRequest	= new MessageParseFromRequest[] {
			// ERROR Message
			null,
			// NULL Message
			null,
			// Text Message
			new MessageParseFromRequest() {
				public Message parse(Request request) {
					Parameters param = request.getParameters();
					String messageID = param.getParameter("msgID");
					String queueName = param.getParameter("queueName");
					String content = param.getParameter("content");
					TextMessage message = new TextMessage(messageID, queueName, content);

					return message;
				}
			}, new MessageParseFromRequest() {
				public Message parse(Request request) {
					Parameters param = request.getParameters();
					String messageID = param.getParameter("msgID");
					String queueName = param.getParameter("queueName");
					BufferedOutputStream outputStream = (BufferedOutputStream) request.getSession()
							.getServerOutputStream();
					byte[] content = outputStream.toByteArray();
					return new ByteMessage(messageID, queueName, content);
				}
			}										};

	MQContextImpl() {
	}

	public Message browser(String messageID) {
		return messageIDs.get(messageID);
	}

	protected void doStart() throws Exception {

		p2pProductLine.start();
		
		subProductLine.start();

		Thread p2pThread = new Thread(p2pProductLine, "JMS-P2P-ProductLine");
		
		Thread subThread = new Thread(subProductLine, "JMS-SUB-ProductLine");

		p2pThread.start();
		
		subThread.start();

	}

	protected void doStop() throws Exception {
		LifeCycleUtil.stop(p2pProductLine);
		LifeCycleUtil.stop(subProductLine);

	}

	public long getMessageDueTime() {
		return this.dueTime;
	}

	public boolean isLogined(Session session) {
		return LOGINED == session.getEndpointMark();
	}

	public int messageSize() {
		return this.messageIDs.size();
	}

	public boolean offerMessage(Message message) {

		synchronized (messageIDs) {
			messageIDs.put(message.getMsgID(), message);
		}

		return p2pProductLine.offerMessage(message);
	}
	
	public boolean publishMessage(Message message){
		
		return subProductLine.offerMessage(message);
	}

	public void consumerMessage(Message message) {
		synchronized (messageIDs) {
			messageIDs.remove(message.getMsgID());
		}
	}

	public Message parse(Request request) {
		Parameters param = request.getParameters();
		int msgType = param.getIntegerParameter("msgType");
		Message message = messageParsesFromRequest[msgType].parse(request);
		return message;
	}

	public void pollMessage(Request request, Response response, JMSSessionAttachment attachment) {

		p2pProductLine.pollMessage(request, response, attachment);
	}
	
	public void subscribeMessage(Request request, Response response, JMSSessionAttachment attachment) {

		subProductLine.pollMessage(request, response, attachment);
	}

	public void setLogined(boolean logined, Session session) {
		session.setEndpointMark(LOGINED);
	}

	public void setMessageDueTime(long dueTime) {
		this.dueTime = dueTime;
		this.p2pProductLine.setDueTime(dueTime);
	}

}
