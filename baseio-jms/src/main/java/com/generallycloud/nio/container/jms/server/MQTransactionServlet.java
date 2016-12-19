package com.generallycloud.nio.container.jms.server;

import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.RESMessage;

public class MQTransactionServlet extends MQServlet {

	public static final String	SERVICE_NAME	= MQTransactionServlet.class.getSimpleName();

	@Override
	public void doAccept(SocketSession session, ProtobaseReadFuture future, MQSessionAttachment attachment) throws Exception {

		String action = future.getReadText();

		TransactionSection section = attachment.getTransactionSection();

		if ("begin".equals(action)) {
			RESMessage message = null;
			if (section == null) {
				section = new TransactionSection(getMQContext());
				attachment.setTransactionSection(section);
				message = RESMessage.SUCCESS;
			} else {
				message = MQRESMessage.R_TRANSACTION_BEGINED;
			}
			future.write(message.toString());
			session.flush(future);

		} else if ("commit".equals(action)) {
			RESMessage message = null;
			if (section == null) {
				message = MQRESMessage.R_TRANSACTION_NOT_BEGIN;
			} else {
				if (section.commit()) {
					message = RESMessage.SUCCESS;
				} else {
					message = MQRESMessage.R_TRANSACTION_NOT_BEGIN;
				}
				attachment.setTransactionSection(null);
			}

			future.write(message.toString());
			session.flush(future);

		} else if ("rollback".equals(action)) {
			RESMessage message = null;
			if (section == null) {
				message = MQRESMessage.R_TRANSACTION_NOT_BEGIN;
			} else {
				if (section.rollback()) {
					message = RESMessage.SUCCESS;
				} else {
					message = MQRESMessage.R_TRANSACTION_NOT_BEGIN;
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
			future.write(MQRESMessage.R_CMD_NOT_FOUND.toString());
			session.flush(future);
		}
	}

}
