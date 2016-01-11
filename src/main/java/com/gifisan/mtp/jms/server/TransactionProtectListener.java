package com.gifisan.mtp.jms.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gifisan.mtp.server.session.Session;
import com.gifisan.mtp.server.session.SessionEventListener;

public class TransactionProtectListener implements SessionEventListener{

	private Logger logger = LoggerFactory.getLogger(TransactionProtectListener.class);
	
	public void onDestroy(Session session) {
		
		Consumer consumer = (Consumer) session.getAttribute("_consumer");
		
		if (consumer != null) {
			MQContext context = MQContextFactory.getMQContext();
			context.removeConsumer(consumer);
		}
		
		TransactionSection section = (TransactionSection) session.getAttribute("_MQ_TRANSACTION");
		if (section != null) {
			section.rollback();
		}
		logger.info(">> TransactionProtectListener execute");
		System.out.println(">> TransactionProtectListener execute");
		
		
		
	}

	

	
	
}
