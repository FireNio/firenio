package com.gifisan.mtp.jms.server;

import com.gifisan.mtp.component.RESMessage;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;
import com.gifisan.mtp.server.session.Session;

public class JMSTransactionServlet extends JMSServlet{

//	private final Logger logger = LoggerFactory.getLogger(JMSConsumerServlet.class);
	
	public void accept(Request request, Response response) throws Exception {

		Session session = request.getSession();
		
		MQContext context = getMQContext();
		
		if (context.isLogined(session)) {
			String action = request.getContent();
			
			TransactionSection section = (TransactionSection) session.getAttribute("_MQ_TRANSACTION");
			
			if ("begin".equals(action)) {
				RESMessage message = null;
				if (section == null) {
					section = new TransactionSection(context);
					session.setAttribute("_MQ_TRANSACTION", section);
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
					session.removeAttribute("_MQ_TRANSACTION");
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
					session.removeAttribute("_MQ_TRANSACTION");
				}
				response.write(message.toString());
				response.flush();
			}else if("complete".equals(action)){
				RESMessage message = RESMessage.R_SUCCESS;
				session.removeAttribute("_TPL_MESSAGE");
				response.write(message.toString());
				response.flush();
				
			}else{
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
