package com.gifisan.nio.plugin.jms.server;

import java.util.List;

import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.SessionEventListener;

public class TransactionProtectListener implements SessionEventListener {
	
	private MQContext context = null;
	
	protected TransactionProtectListener(MQContext context) {
		this.context = context;
	}

	//FIXME 移除该EndPoint上所有的consumer
	public void onDestroy(Session session) {

		MQContext context = this.context;
		
		JMSSessionAttachment attachment = (JMSSessionAttachment) session.getAttachment(context);

		TransactionSection section = attachment.getTransactionSection();

		if (section != null) {

			section.rollback();
		}
		
		List<String> queueNames = attachment.getQueueNames();
		
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
