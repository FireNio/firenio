package com.gifisan.nio.jms.server;

import java.util.HashMap;
import java.util.Map;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.component.MessageWriterJob;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.concurrent.BlockingQueueThreadPool;
import com.gifisan.nio.concurrent.ThreadPool;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.server.Request;
import com.gifisan.nio.server.Response;

public class P2PProductLine extends AbstractLifeCycle implements MessageQueue, Runnable {

	private ThreadPool				messageWriteThreadPool	= null;
	private Map<String, ConsumerQueue>	consumerGroupMap		= null;
	private MQContext				context				= null;
	private MessageQ			messageGroup			= null;
	private boolean				running				= false;
	private long					dueTime				= 0;

	public P2PProductLine(MQContext context) {
		this.context = context;
	}

	protected void doStart() throws Exception {

		this.running = true;

		this.messageGroup = new MessageQ();

		this.consumerGroupMap = new HashMap<String, ConsumerQueue>();

		//TODO ..... set dueTime
		this.dueTime = context.getMessageDueTime();

		this.messageWriteThreadPool = new BlockingQueueThreadPool("Message-writer", 1);

		this.messageWriteThreadPool.start();

	}

	// TODO 处理剩下的message 和 receiver
	protected void doStop() throws Exception {

		this.running = false;

		LifeCycleUtil.stop(messageWriteThreadPool);
	}

	private ConsumerQueue getConsumerGroup(String queueName) {

		ConsumerQueue consumerGroup = consumerGroupMap.get(queueName);

		if (consumerGroup == null) {

			synchronized (consumerGroupMap) {

				consumerGroup = consumerGroupMap.get(queueName);

				if (consumerGroup == null) {
					consumerGroup = new ConsumerQueue();
					consumerGroupMap.put(queueName, consumerGroup);
				}
			}
		}
		return consumerGroup;
	}

	public MQContext getContext() {
		return context;
	}

	public boolean offerMessage(Message message) {
		return messageGroup.offer(message);
	}

	public void pollMessage(Request request, Response response,JMSSessionAttachment attachment) {

		Parameters param = request.getParameters();

		String queueName = param.getParameter("queueName");

		ConsumerQueue consumerGroup = getConsumerGroup(queueName);
		
		Consumer consumer = new Consumer(consumerGroup, attachment,response, queueName);
		
		consumerGroup.offer(consumer);
	}

	public void removeConsumer(Consumer consumer) {

		ConsumerQueue consumerGroup = consumer.getConsumerGroup();

		consumerGroup.remove(consumer);

		//FIXME 这样做不安全，出现问题的可能性比较小，但是以后再想办法解决
		if (consumerGroup.size() == 0) {
			synchronized (consumerGroupMap) {
				if (consumerGroup.size() == 0) {
					consumerGroupMap.remove(consumer.getQueueName());
				}
			}
		}
	}

	public void run() {
		
		for (;running;) {

			Message message = messageGroup.poll(16);

			if (message == null) {
				continue;
			}

			String queueName = message.getQueueName();

			ConsumerQueue consumerGroup = getConsumerGroup(queueName);

			Consumer consumer = consumerGroup.poll(16);

			if (consumer == null) {

				filterUseless(message);

				continue;
			}

			MessageWriterJob job = new MessageWriterJob(context,consumer, message);

			messageWriteThreadPool.dispatch(job);

		}

	}

	private void filterUseless(Message message) {
		long now = System.currentTimeMillis();
		long dueTime = this.dueTime;

		if (now - message.getTimestamp() < dueTime) {
			this.offerMessage(message);
		}
		// 消息过期了

	}

	public void setDueTime(long dueTime) {
		this.dueTime = dueTime;
	}

}
