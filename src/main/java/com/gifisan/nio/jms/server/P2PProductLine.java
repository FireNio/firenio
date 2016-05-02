package com.gifisan.nio.jms.server;

import com.gifisan.nio.jms.Message;

public class P2PProductLine extends AbstractProductLine implements MessageQueue, Runnable {


	public P2PProductLine(MQContext context) {
		super(context);
	}
	
	protected ConsumerQueue createConsumerQueue() {
		return new P2PConsumerQueue();
	}

	//FIXME 完善消息匹配机制
	public void run() {

		for (; running;) {

			Message message = storage.poll(16);

			if (message == null) {
				continue;
			}

			String queueName = message.getQueueName();

			ConsumerQueue consumerQueue = getConsumerQueue(queueName);

			Consumer consumer = consumerQueue.poll(16);

			if (consumer == null) {

				filterUseless(message);

				continue;
			}

			consumer.push(message);

			context.consumerMessage(message);
		}
	}
}
