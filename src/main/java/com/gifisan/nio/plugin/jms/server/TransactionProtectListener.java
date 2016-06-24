package com.gifisan.nio.plugin.jms.server;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.SessionEventListener;

public class TransactionProtectListener implements SessionEventListener {

	private static final Logger	LOGGER	= LoggerFactory.getLogger(TransactionProtectListener.class);

	public void sessionOpened(Session session) {

	}

	// FIXME 移除该EndPoint上的consumer
	public void sessionClosed(Session session) {
		MQContext context = MQContextFactory.getMQContext();

		JMSSessionAttachment attachment = (JMSSessionAttachment) session.getAttachment(context);

		TransactionSection section = attachment.getTransactionSection();

		if (section != null) {

			section.rollback();
		}

		Consumer consumer = attachment.getConsumer();

		if (consumer != null) {

			consumer.getConsumerQueue().remove(consumer);

			consumer.getConsumerQueue().getSnapshot();

			context.removeReceiver(consumer.getQueueName() + session.getMachineType());
		}

		LOGGER.debug(">>>> TransactionProtectListener execute");

	}

}
