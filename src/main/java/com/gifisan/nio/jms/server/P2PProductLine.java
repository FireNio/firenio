package com.gifisan.nio.jms.server;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.server.Request;
import com.gifisan.nio.server.Response;

public class P2PProductLine extends AbstractLifeCycle implements Queue, Runnable {

	private Logger							logger		= LoggerFactory.getLogger(P2PProductLine.class);
	protected Map<String, ConsumerQueue>		consumerMap	= null;
	protected MQContext					context		= null;
	protected MessageQueue					queue		= null;
	protected long						dueTime		= 0;
	protected boolean						running		= false;

	public P2PProductLine(MQContext context) {
		this.context = context;
	}

	protected void doStart() throws Exception {

		this.running = true;

		this.queue = new MessageQueue();

		this.consumerMap = new HashMap<String, ConsumerQueue>();

		// TODO ..... set dueTime
		this.dueTime = context.getMessageDueTime();

	}

	// TODO 处理剩下的message 和 receiver
	protected void doStop() throws Exception {

		this.running = false;
	}

	protected ConsumerQueue getConsumerGroup(String queueName) {

		ConsumerQueue consumerGroup = consumerMap.get(queueName);

		if (consumerGroup == null) {

			synchronized (consumerMap) {

				consumerGroup = consumerMap.get(queueName);

				if (consumerGroup == null) {
					consumerGroup = new ConsumerQueue();
					consumerMap.put(queueName, consumerGroup);
				}
			}
		}
		return consumerGroup;
	}

	public MQContext getContext() {
		return context;
	}

	public boolean offerMessage(Message message) {
		return queue.offer(message);
	}

	public void pollMessage(Request request, Response response, JMSSessionAttachment attachment) {

		Parameters param = request.getParameters();

		String queueName = param.getParameter("queueName");

		ConsumerQueue consumerGroup = getConsumerGroup(queueName);

		Consumer consumer = new Consumer(consumerGroup, attachment, response, queueName);

		consumerGroup.offer(consumer);
	}

	public void run() {

		for (; running;) {

			Message message = queue.poll(16);

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

			try {

				consumer.push(message);

				context.consumerMessage(message);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				// 回炉
				context.offerMessage(message);

			}

		}

	}

	protected void filterUseless(Message message) {
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
