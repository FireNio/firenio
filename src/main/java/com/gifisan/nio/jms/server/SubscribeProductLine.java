package com.gifisan.nio.jms.server;

import com.gifisan.nio.jms.Message;

public class SubscribeProductLine extends AbstractProductLine implements MessageQueue, Runnable {

	public SubscribeProductLine(MQContext context) {
		super(context);
	}

	protected ConsumerQueue createConsumerQueue() {

		return new SUBConsumerQueue();
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

			Consumer[] consumers = consumerQueue.snapshot();

			if (consumers[0] == null) {
				filterUseless(message);
				continue;
			}

			for (int i = 0; i < consumers.length; i++) {

				Consumer consumer = consumers[i];

				if (consumer == null) {
					break;
				}

				consumer.push(message);
			}
			
			context.consumerMessage(message);
		}
	}
}
