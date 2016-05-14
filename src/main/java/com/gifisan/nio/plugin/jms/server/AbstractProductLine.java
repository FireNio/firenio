package com.gifisan.nio.plugin.jms.server;

import java.util.HashMap;
import java.util.Map;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.plugin.jms.Message;
import com.gifisan.nio.server.IOSession;

public abstract class AbstractProductLine extends AbstractLifeCycle implements MessageQueue, Runnable {

	protected MQContext					context		= null;
	protected MessageStorage				storage		= null;
	protected long						dueTime		= 0;
	protected boolean						running		= false;
	protected Map<String, ConsumerQueue>		consumerMap	= null;

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
	
	public void pollMessage(IOSession session,ServerReadFuture future, JMSSessionAttachment attachment) {

		Parameters param = future.getParameters();

		String queueName = param.getParameter("queueName");

		ConsumerQueue consumerQueue = getConsumerQueue(queueName);

		Consumer consumer = new Consumer(consumerQueue, attachment, session,future, queueName);
		
		attachment.addConsumer(consumer);

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
			DebugUtil.debug(">>>> message invalidate : {}",message);
			return;
		}
		this.offerMessage(message);
	}

	public void setDueTime(long dueTime) {
		this.dueTime = dueTime;
	}

}
