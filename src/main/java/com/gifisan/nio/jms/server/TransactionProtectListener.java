package com.gifisan.nio.jms.server;

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

		DebugUtil.debug(">> TransactionProtectListener execute");
	}
}
