package com.gifisan.mtp.jms.server;

import java.util.HashMap;
import java.util.Map;

import com.gifisan.mtp.AbstractLifeCycle;
import com.gifisan.mtp.common.LifeCycleUtil;
import com.gifisan.mtp.common.SharedBundle;
import com.gifisan.mtp.component.BlockingQueueThreadPool;
import com.gifisan.mtp.component.MessageWriterJob;
import com.gifisan.mtp.component.ThreadPool;
import com.gifisan.mtp.jms.Message;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;

public class SubscribeProductLine extends AbstractLifeCycle implements MessageQueue, Runnable {

	private ThreadPool				messageWriteThreadPool	= null;
	private Map<String, ConsumerGroup>	consumerGroupMap		= null;
	private MQContext				context				= null;
	private MessageGroup			messageGroup			= null;
	private boolean				running				= false;
	private long					dueTime				= 0;

	public SubscribeProductLine(MQContext context) {
		this.context = context;
	}

	protected void doStart() throws Exception {

		this.running = true;

		this.messageGroup = new MessageGroup();

		this.consumerGroupMap = new HashMap<String, ConsumerGroup>();

		this.dueTime = context.getMessageDueTime();

		int APP_SERVER_CORE_SIZE = SharedBundle.getIntegerProperty("APP_SERVER_CORE_SIZE");

		APP_SERVER_CORE_SIZE = APP_SERVER_CORE_SIZE == 0 ? 4 : APP_SERVER_CORE_SIZE;

		this.messageWriteThreadPool = new BlockingQueueThreadPool("Message-write-Job", APP_SERVER_CORE_SIZE);

		// this.messageWriteThreadPool = new
		// LinkNodeQueueThreadPool("MessageWrite-Job", APP_SERVER_CORE_SIZE);

		this.messageWriteThreadPool.start();

	}

	// TODO 处理剩下的message 和 receiver
	protected void doStop() throws Exception {

		this.running = false;

		LifeCycleUtil.stop(messageWriteThreadPool);
	}

	private ConsumerGroup getConsumerGroup(String queueName) {

		ConsumerGroup consumerGroup = consumerGroupMap.get(queueName);

		if (consumerGroup == null) {

			synchronized (consumerGroupMap) {

				consumerGroup = consumerGroupMap.get(queueName);

				if (consumerGroup == null) {
					consumerGroup = new ConsumerGroup();
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

	public void pollMessage(Request request, Response response) {

		String queueName = request.getParameter("queueName");

		ConsumerGroup consumerGroup = getConsumerGroup(queueName);

		Consumer consumer = new Consumer(request, response);

		consumerGroup.offer(consumer);
	}

	public void run() {

		while (running) {

			Message message = messageGroup.poll(16);

			if (message == null) {
				continue;
			}

			String queueName = message.getQueueName();

			ConsumerGroup consumerGroup = getConsumerGroup(queueName);

			Consumer consumer = consumerGroup.poll(16);

			if (consumer == null) {

				filterUseless(message);

				continue;
			}

			MessageWriterJob job = new MessageWriterJob(messageGroup, consumer, message);

			messageWriteThreadPool.dispatch(job);

			// try {
			// consumer.push(message);
			// } catch (IOException e) {
			// // TODO roll back
			// e.printStackTrace();
			// offerMessage(message);
			// }

		}

	}

	private void filterUseless(Message message) {
		long now = System.currentTimeMillis();
		long dueTime = this.dueTime;

		if (now - message.createTime() < dueTime) {
			this.offerMessage(message);
		}
		// 消息过期了

	}

}
