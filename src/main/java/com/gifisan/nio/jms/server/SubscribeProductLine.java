package com.gifisan.nio.jms.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gifisan.nio.jms.Message;

public class SubscribeProductLine extends P2PProductLine implements Queue, Runnable {

	public SubscribeProductLine(MQContext context) {
		super(context);
	}

	private Logger			logger	= LoggerFactory.getLogger(P2PProductLine.class);

	public void run() {

		for (; running;) {

			Message message = queue.poll(16);

			if (message == null) {
				continue;
			}

			String queueName = message.getQueueName();

			ConsumerQueue consumerGroup = getConsumerGroup(queueName);

			
			Consumer consumer = consumerGroup.poll(16);

			for(;consumer!=null;){
				
				filterUseless(message);
				
				try {

					consumer.push(message);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
				consumer = consumerGroup.poll(16);
			}
		}
	}
}
