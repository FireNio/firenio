package com.gifisan.nio.jms.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gifisan.nio.server.session.Session;
import com.gifisan.nio.server.session.SessionEventListener;

public class TransactionProtectListener implements SessionEventListener{

	private Logger logger = LoggerFactory.getLogger(TransactionProtectListener.class);
	
	public void onDestroy(Session session) {
		
		JMSSessionAttachment attachment = (JMSSessionAttachment) session.attachment();
		
		TransactionSection section = attachment.getTransactionSection();
		
		if (section != null) {
			
			section.rollback();
			
		}
		
		logger.info(">> TransactionProtectListener execute");
		
		System.out.println(">> TransactionProtectListener execute");
		
	}

}
