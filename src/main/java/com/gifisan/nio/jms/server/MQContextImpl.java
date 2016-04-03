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

public class MQContextImpl extends AbstractLifeCycle implements MQContext {

	
	private interface MessageParseFromRequest {

		Message parse(Request request);
	}
	private long					dueTime		= 0;
	private final int				LOGINED		= 1;
	private HashMap<String, Message>	messageIDs	= new HashMap<String, Message>();
	private P2PProductLine			productLine	= new P2PProductLine(this);

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
		},
		new MessageParseFromRequest() {
			public Message parse(Request request) {
				Parameters param = request.getParameters();
				String messageID = param.getParameter("msgID");
				String queueName = param.getParameter("queueName");
				BufferedOutputStream outputStream = (BufferedOutputStream) request.getSession().getServerOutputStream();
				byte[] content = outputStream.toByteArray();
				return new ByteMessage(messageID, queueName, content);
			}
		}
	};

	

	MQContextImpl() {}

	public Message browser(String messageID) {

		return messageIDs.get(messageID);
	}

	protected void doStart() throws Exception {

		productLine.start();

		Thread lineThread = new Thread(productLine, "Message-product-line");

		lineThread.start();

	}

	protected void doStop() throws Exception {
		LifeCycleUtil.stop(productLine);

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

		return productLine.offerMessage(message);
	}
	
	public void consumerMessage(Message message){
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

	public void pollMessage(Request request, Response response,JMSSessionAttachment attachment) {

		productLine.pollMessage(request, response,attachment);

	}

	public void setLogined(boolean logined, Session session) {
		session.setEndpointMark(LOGINED);
	}

	public void setMessageDueTime(long dueTime) {
		this.dueTime = dueTime;
		this.productLine.setDueTime(dueTime);
	}

	public void removeConsumer(Consumer consumer) {
		this.productLine.removeConsumer(consumer);
		
	}

	
	

}
