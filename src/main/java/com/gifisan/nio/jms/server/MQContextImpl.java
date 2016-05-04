package com.gifisan.nio.jms.server;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantLock;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.component.LoginCenter;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.jms.JMSException;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.jms.decode.DefaultMessageDecoder;
import com.gifisan.nio.jms.decode.MessageDecoder;
import com.gifisan.nio.server.IOSession;

public class MQContextImpl extends AbstractLifeCycle implements MQContext {

	private long					dueTime				= 0;
	private HashMap<String, Message>	messageIDs			= new HashMap<String, Message>();
	private P2PProductLine			p2pProductLine			= new P2PProductLine(this);
	private SubscribeProductLine		subProductLine			= new SubscribeProductLine(this);
	private HashSet<String>			receivers				= new HashSet<String>();
	private LoginCenter				loginCenter			= null;
	private MessageDecoder			messageDecoder			= new DefaultMessageDecoder();
	private ReentrantLock			messageIDsLock			= new ReentrantLock();
	private ReentrantLock			reveiversLock			= new ReentrantLock();
	private ConsumerPushHandle		consumerPushFailedHandle	= null;

	MQContextImpl() {
	}

	public Message browser(String messageID) {
		return messageIDs.get(messageID);
	}

	protected void doStart() throws Exception {

		Thread p2pThread = new Thread(p2pProductLine, "JMS-P2P-ProductLine");

		Thread subThread = new Thread(subProductLine, "JMS-SUB-ProductLine");

		this.loginCenter = new DefaultJMSLoginCenter();
		
		this.consumerPushFailedHandle = new ConsumerPushHandle(this);

		loginCenter.start();

		p2pProductLine.start();

		subProductLine.start();

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

	public int messageSize() {
		return this.p2pProductLine.messageSize();
	}

	public void offerMessage(Message message) {

		ReentrantLock lock = this.messageIDsLock;

		lock.lock();

		messageIDs.put(message.getMsgID(), message);

		lock.unlock();

		p2pProductLine.offerMessage(message);
	}

	public void publishMessage(Message message) {

		subProductLine.offerMessage(message);
	}

	public void consumerMessage(Message message) {
		ReentrantLock lock = this.messageIDsLock;

		lock.lock();

		messageIDs.remove(message.getMsgID());

		lock.unlock();
	}

	public Message parse(ReadFuture future) throws JMSException {
		return messageDecoder.decode(future);
	}

	public void pollMessage(IOSession session, ServerReadFuture future, JMSSessionAttachment attachment) {

		p2pProductLine.pollMessage(session, future, attachment);
	}

	public void subscribeMessage(IOSession session, ServerReadFuture future, JMSSessionAttachment attachment) {

		subProductLine.pollMessage(session, future, attachment);
	}

	public void setMessageDueTime(long dueTime) {
		this.dueTime = dueTime;
		this.p2pProductLine.setDueTime(dueTime);
		this.subProductLine.setDueTime(dueTime);
	}

	public void addReceiver(String queueName) {
		ReentrantLock lock = this.reveiversLock;

		lock.lock();
		receivers.add(queueName);
		lock.unlock();
	}

	public boolean isOnLine(String queueName) {
		return receivers.contains(queueName);
	}

	public void removeReceiver(String queueName) {
		ReentrantLock lock = this.reveiversLock;

		lock.lock();

		receivers.remove(queueName);

		lock.unlock();
	}

	public LoginCenter getLoginCenter() {
		return loginCenter;
	}

	public void reload() {
		LifeCycleUtil.stop(loginCenter);

		LifeCycleUtil.start(loginCenter);
	}

	public ConsumerPushHandle getConsumerPushFailedHandle() {
		return consumerPushFailedHandle;
	}

}
