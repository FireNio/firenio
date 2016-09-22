package com.generallycloud.nio.extend.plugin.jms.server;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.SEListenerAdapter;
import com.generallycloud.nio.component.Session;

public class MQSessionEventListener extends SEListenerAdapter {

	private static final Logger	LOGGER	= LoggerFactory.getLogger(MQSessionEventListener.class);

	public void sessionOpened(Session session) {
		
		MQContext context = MQContext.getInstance();

		MQSessionAttachment attachment = context.getSessionAttachment(session);

		if (attachment == null) {

			attachment = new MQSessionAttachment(context);

			session.setAttachment(context.getPluginIndex(), attachment);
		}
	}

	// FIXME 移除该session上的consumer
	public void sessionClosed(Session session) {
		
		MQContext context = MQContext.getInstance();

		MQSessionAttachment attachment = context.getSessionAttachment(session);
		
		if (attachment == null) {
			return;
		}

		TransactionSection section = attachment.getTransactionSection();

		if (section != null) {

			section.rollback();
		}

		Consumer consumer = attachment.getConsumer();

		if (consumer != null) {

			consumer.getConsumerQueue().remove(consumer);

			consumer.getConsumerQueue().getSnapshot();

			context.removeReceiver(consumer.getQueueName());
		}

		LOGGER.debug(">>>> TransactionProtectListener execute");

	}

}
