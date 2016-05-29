package com.gifisan.nio.plugin.jms.server;

import java.util.List;

import com.gifisan.nio.plugin.jms.Message;

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

			List<Consumer> consumers = consumerQueue.snapshot();

			if (consumers.size() == 0) {

				filterUseless(message);

				continue;
			}

			for(Consumer consumer:consumers){
				consumer.push(message);
			}

			context.consumerMessage(message);
		}
	}
	
	public int messageSize(){
		return storage.size();
	}
}
