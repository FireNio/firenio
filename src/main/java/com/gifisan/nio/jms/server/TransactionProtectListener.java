package com.gifisan.nio.jms.server;

import java.util.List;

import com.gifisan.nio.common.DebugUtil;
import com.gifisan.nio.server.session.Session;
import com.gifisan.nio.server.session.SessionEventListener;

public class TransactionProtectListener implements SessionEventListener {

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
		
		DebugUtil.debug(">> TransactionProtectListener execute");
	}
}
