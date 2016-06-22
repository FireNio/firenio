package com.gifisan.nio.plugin.jms.server;

import java.util.HashMap;
import java.util.Map;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.ApplicationContextUtil;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.plugin.jms.Message;
import com.gifisan.security.Authority;

public abstract class AbstractProductLine extends AbstractLifeCycle implements MessageQueue, Runnable {

	protected MQContext					context		= null;
	protected MessageStorage				storage		= null;
	protected long						dueTime		= 0;
	protected boolean						running		= false;
	protected Map<String, ConsumerQueue>		consumerMap	= null;
	private Logger							logger		= LoggerFactory.getLogger(AbstractProductLine.class);

	public AbstractProductLine(MQContext context) {
		this.context = context;
	}

	protected void doStart() throws Exception {

		this.running = true;

		this.storage = new MessageStorage();

		this.consumerMap = new HashMap<String, ConsumerQueue>();

		this.dueTime = context.getMessageDueTime();

	}

	// TODO 处理剩下的message 和 receiver
	protected void doStop() throws Exception {
		this.running = false;
	}

	public MQContext getContext() {
		return context;
	}

	public void pollMessage(Session session, ReadFuture future, JMSSessionAttachment attachment) {

		if (attachment.getConsumer() != null) {
			return;
		}
		
		Authority authority = ApplicationContextUtil.getAuthority(session);

		String queueName = authority.getUuid();

		// 来自终端类型
		context.addReceiver(queueName+session.getMachineType());
		
		ConsumerQueue consumerQueue = getConsumerQueue(queueName);

		Consumer consumer = new Consumer(consumerQueue, attachment, session, future, queueName);
		
		attachment.setConsumer(consumer);

		//FIXME 按位置设置
		consumerQueue.offer(consumer);
	}

	protected ConsumerQueue getConsumerQueue(String queueName) {

		ConsumerQueue consumerQueue = consumerMap.get(queueName);

		if (consumerQueue == null) {

			synchronized (consumerMap) {

				consumerQueue = consumerMap.get(queueName);

				if (consumerQueue == null) {
					consumerQueue = createConsumerQueue();
					consumerMap.put(queueName, consumerQueue);
				}
			}
		}
		return consumerQueue;
	}

	protected abstract ConsumerQueue createConsumerQueue();

	public void offerMessage(Message message) {

		storage.offer(message);
	}

	protected void filterUseless(Message message) {
		long now = System.currentTimeMillis();
		long dueTime = this.dueTime;

		if (now - message.getTimestamp() > dueTime) {
			// 消息过期了
			logger.debug(">>>> message invalidate : {}", message);
			return;
		}
		this.offerMessage(message);
	}

	public void setDueTime(long dueTime) {
		this.dueTime = dueTime;
	}

}
