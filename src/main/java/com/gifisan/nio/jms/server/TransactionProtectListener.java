package com.gifisan.nio.jms.server;

import java.util.List;

import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.SessionEventListener;

public class TransactionProtectListener implements SessionEventListener {

	//FIXME 移除该EndPoint上所有的consumer
	public void onDestroy(Session session) {

		JMSSessionAttachment attachment = (JMSSessionAttachment) session.attachment();

		TransactionSection section = attachment.getTransactionSection();

		if (section != null) {

			section.rollback();
		}
		
		List<String> queueNames = attachment.getQueueNames();
		
		MQContext context = attachment.getContext();
		
		for(String queueName :queueNames){
			context.removeReceiver(queueName);
		}
		
		List<Consumer> consumers = attachment.getConsumers();
		
		if (consumers.size() > 0) {
			
			consumers.get(0).getConsumerQueue().remove(consumers);
		}
		
		DebugUtil.debug(">>>> TransactionProtectListener execute");
	}
}
