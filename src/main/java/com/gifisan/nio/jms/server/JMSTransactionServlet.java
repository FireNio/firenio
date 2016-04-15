package com.gifisan.nio.jms.server;

import com.gifisan.nio.component.RESMessage;
import com.gifisan.nio.server.session.Session;
import com.gifisan.nio.service.Request;
import com.gifisan.nio.service.Response;

public class JMSTransactionServlet extends JMSServlet{

	public void accept(Request request, Response response,JMSSessionAttachment attachment) throws Exception {

		Session session = request.getSession();
		
		MQContext context = getMQContext();
		
		if (context.isLogined(session)) {
			String action = request.getContent();
			
			TransactionSection section = attachment.getTransactionSection();
			
			if ("begin".equals(action)) {
				RESMessage message = null;
				if (section == null) {
					section = new TransactionSection(context);
					attachment.setTransactionSection(section);
					message = RESMessage.R_SUCCESS;
				}else{
					message = JMSRESMessage.R_TRANSACTION_BEGINED;
				}
				response.write(message.toString());
				response.flush();
				
			}else if("commit".equals(action)){
				RESMessage message = null;
				if (section == null) {
					message = JMSRESMessage.R_TRANSACTION_NOT_BEGIN;
				}else{
					if (section.commit()) {
						message = RESMessage.R_SUCCESS;
					}else{
						message = JMSRESMessage.R_TRANSACTION_NOT_BEGIN;
					}
					attachment.setTransactionSection(null);
				}
				
				response.write(message.toString());
				response.flush();
				
			}else if("rollback".equals(action)){
				RESMessage message = null;
				if (section == null) {
					message = JMSRESMessage.R_TRANSACTION_NOT_BEGIN;
				}else{
					if (section.rollback()) {
						message = RESMessage.R_SUCCESS;
					}else{
						message = JMSRESMessage.R_TRANSACTION_NOT_BEGIN;
					}
					attachment.setTransactionSection(null);
				}
				response.write(message.toString());
				response.flush();
			}
//			else if("complete".equals(action)){
//				RESMessage message = RESMessage.R_SUCCESS;
//				attachment.setTpl_message(null);
//				response.write(message.toString());
//				response.flush();
//				
//			}
			else{
				response.write(JMSRESMessage.R_CMD_NOT_FOUND.toString());
				response.flush();
			}
		}else{
			RESMessage message = JMSRESMessage.R_UNAUTH;
			response.write(message.toString());
			response.flush();
		}
	}
	
}
