package com.gifisan.nio.jms.server;


import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.jms.Message;

public class SubscribeProductLine extends P2PProductLine implements MessageQueue, Runnable {

	public SubscribeProductLine(MQContext context) {
		super(context);
	}

	private Logger			logger	= LoggerFactory.getLogger(P2PProductLine.class);

	public void run() {

		for (; running;) {

			Message message = storage.poll(16);

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
