package com.gifisan.nio.plugin.jms.server;

import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.IOSession;
import com.gifisan.nio.server.RESMessage;

public class JMSTransactionServlet extends JMSServlet {

	public static final String	SERVICE_NAME	= JMSTransactionServlet.class.getSimpleName();

	public void accept(IOSession session, ServerReadFuture future, JMSSessionAttachment attachment) throws Exception {

		String action = future.getText();

		TransactionSection section = attachment.getTransactionSection();

		if ("begin".equals(action)) {
			RESMessage message = null;
			if (section == null) {
				section = new TransactionSection(getMQContext());
				attachment.setTransactionSection(section);
				message = RESMessage.R_SUCCESS;
			} else {
				message = JMSRESMessage.R_TRANSACTION_BEGINED;
			}
			future.write(message.toString());
			session.flush(future);

		} else if ("commit".equals(action)) {
			RESMessage message = null;
			if (section == null) {
				message = JMSRESMessage.R_TRANSACTION_NOT_BEGIN;
			} else {
				if (section.commit()) {
					message = RESMessage.R_SUCCESS;
				} else {
					message = JMSRESMessage.R_TRANSACTION_NOT_BEGIN;
				}
				attachment.setTransactionSection(null);
			}

			future.write(message.toString());
			session.flush(future);

		} else if ("rollback".equals(action)) {
			RESMessage message = null;
			if (section == null) {
				message = JMSRESMessage.R_TRANSACTION_NOT_BEGIN;
			} else {
				if (section.rollback()) {
					message = RESMessage.R_SUCCESS;
				} else {
					message = JMSRESMessage.R_TRANSACTION_NOT_BEGIN;
				}
				attachment.setTransactionSection(null);
			}
			future.write(message.toString());
			session.flush(future);
		}
		// else if("complete".equals(action)){
		// RESMessage message = RESMessage.R_SUCCESS;
		// attachment.setTpl_message(null);
		// response.write(message.toString());
		// response.flush();
		//
		// }
		else {
			future.write(JMSRESMessage.R_CMD_NOT_FOUND.toString());
			session.flush(future);
		}
	}

}
