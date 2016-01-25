package com.gifisan.mtp.jms.server;

import java.util.HashMap;

import com.gifisan.mtp.AbstractLifeCycle;
import com.gifisan.mtp.common.LifeCycleUtil;
import com.gifisan.mtp.component.RequestParam;
import com.gifisan.mtp.jms.Message;
import com.gifisan.mtp.jms.TextMessage;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;
import com.gifisan.mtp.server.session.Session;

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
				RequestParam param = request.getParameters();
				String messageID = param.getParameter("messageID");
				String queueName = param.getParameter("queueName");
				String content = param.getParameter("content");
				TextMessage message = new TextMessage(messageID, queueName, content);

				return message;
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
		// TODO 。。。。
		return 0;
	}

	public boolean offerMessage(Message message) {

		synchronized (messageIDs) {
			messageIDs.put(message.getMessageID(), message);
		}

		return productLine.offerMessage(message);
	}

	public Message parse(Request request) {
		RequestParam param = request.getParameters();
		int msgType = param.getIntegerParameter("msgType");
		Message message = messageParsesFromRequest[msgType].parse(request);
		return message;
	}

	public void pollMessage(Request request, Response response) {

		productLine.pollMessage(request, response);

	}

	public void setLogined(boolean logined, Session session) {
		session.setEndpointMark(LOGINED);
	}

	public void setMessageDueTime(long dueTime) {
		this.dueTime = dueTime;
	}

	public void removeConsumer(Consumer consumer) {
		this.productLine.removeConsumer(consumer);
		
	}

	
	

}
