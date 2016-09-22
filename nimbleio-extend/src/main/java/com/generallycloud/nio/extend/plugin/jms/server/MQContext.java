package com.generallycloud.nio.extend.plugin.jms.server;

import java.util.Map;

import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.concurrent.ReentrantMap;
import com.generallycloud.nio.component.concurrent.ReentrantSet;
import com.generallycloud.nio.component.concurrent.EventLoopThread;
import com.generallycloud.nio.component.protocol.nio.future.NIOReadFuture;
import com.generallycloud.nio.extend.AbstractPluginContext;
import com.generallycloud.nio.extend.ApplicationContext;
import com.generallycloud.nio.extend.configuration.Configuration;
import com.generallycloud.nio.extend.plugin.jms.MQException;
import com.generallycloud.nio.extend.plugin.jms.Message;
import com.generallycloud.nio.extend.plugin.jms.decode.DefaultMessageDecoder;
import com.generallycloud.nio.extend.plugin.jms.decode.MessageDecoder;
import com.generallycloud.nio.extend.service.FutureAcceptorService;

public class MQContext extends AbstractPluginContext implements MessageQueue {

	private long						dueTime;
	private ReentrantMap<String, Message>	messageIDs	= new ReentrantMap<String, Message>();
	private P2PProductLine				p2pProductLine	= new P2PProductLine(this);
	private SubscribeProductLine			subProductLine	= new SubscribeProductLine(this);
	private ReentrantSet<String>			receivers		= new ReentrantSet<String>();
	private MessageDecoder				messageDecoder	= new DefaultMessageDecoder();
	private static MQContext			instance;
	private EventLoopThread				p2pThread;
	private EventLoopThread				subThread;

	public static MQContext getInstance() {
		return instance;
	}

	public Message browser(String messageID) {
		return messageIDs.get(messageID);
	}

	public MQSessionAttachment getSessionAttachment(Session session) {
		return (MQSessionAttachment) session.getAttachment(this.getPluginIndex());
	}

	public void initialize(ApplicationContext context, Configuration config) throws Exception {

		long dueTime = config.getLongParameter("due-time");

		setMessageDueTime(dueTime == 0 ? 1000 * 60 * 60 * 24 * 7 : dueTime);

		p2pThread = new EventLoopThread(p2pProductLine, "MQ-P2P-ProductLine");

		subThread = new EventLoopThread(subProductLine, "MQ-SUB-ProductLine");

		p2pThread.start();
		subThread.start();

		context.addSessionEventListener(new MQSessionEventListener());

		instance = this;
	}

	public void destroy(ApplicationContext context, Configuration config) throws Exception {
		LifeCycleUtil.stop(p2pThread);
		LifeCycleUtil.stop(subThread);
		instance = null;
		super.destroy(context, config);
	}

	public long getMessageDueTime() {
		return this.dueTime;
	}

	public int messageSize() {
		return this.p2pProductLine.messageSize();
	}

	public void offerMessage(Message message) {

		messageIDs.put(message.getMsgID(), message);

		p2pProductLine.offerMessage(message);
	}

	public void publishMessage(Message message) {

		subProductLine.offerMessage(message);
	}

	public void consumerMessage(Message message) {

		messageIDs.remove(message.getMsgID());
	}

	public Message parse(NIOReadFuture future) throws MQException {
		return messageDecoder.decode(future);
	}

	public void pollMessage(Session session, NIOReadFuture future, MQSessionAttachment attachment) {

		p2pProductLine.pollMessage(session, future, attachment);
	}

	public void subscribeMessage(Session session, NIOReadFuture future, MQSessionAttachment attachment) {

		subProductLine.pollMessage(session, future, attachment);
	}

	public void setMessageDueTime(long dueTime) {
		this.dueTime = dueTime;
		this.p2pProductLine.setDueTime(dueTime);
		this.subProductLine.setDueTime(dueTime);
	}

	public void addReceiver(String queueName) {
		receivers.add(queueName);
	}

	public boolean isOnLine(String queueName) {
		return receivers.contains(queueName);
	}

	public void removeReceiver(String queueName) {
		receivers.remove(queueName);
	}

	public void configFutureAcceptor(Map<String, FutureAcceptorService> acceptors) {

		acceptors.put(MQConsumerServlet.SERVICE_NAME, new MQConsumerServlet());
		acceptors.put(MQProducerServlet.SERVICE_NAME, new MQProducerServlet());
		acceptors.put(MQSubscribeServlet.SERVICE_NAME, new MQSubscribeServlet());
		acceptors.put(MQPublishServlet.SERVICE_NAME, new MQPublishServlet());
		acceptors.put(MQTransactionServlet.SERVICE_NAME, new MQTransactionServlet());
		acceptors.put(MQBrowserServlet.SERVICE_NAME, new MQBrowserServlet());

	}

	public void prepare(ApplicationContext context, Configuration config) throws Exception {

		// MQContext old = getInstance();

		// FIXME 把老的Context中的数据放到这里

		long dueTime = config.getLongParameter("due-time");

		setMessageDueTime(dueTime == 0 ? 1000 * 60 * 60 * 24 * 7 : dueTime);

		p2pThread = new EventLoopThread(p2pProductLine, "MQ-P2P-ProductLine");

		subThread = new EventLoopThread(subProductLine, "MQ-SUB-ProductLine");

		p2pThread.start();
		subThread.start();

		instance = this;
	}

	public void unload(ApplicationContext context, Configuration config) throws Exception {
		LifeCycleUtil.stop(p2pThread);
		LifeCycleUtil.stop(subThread);
		super.destroy(context, config);
	}

}
