package com.gifisan.mtp.jms.server;

import com.gifisan.mtp.jms.ErrorMessage;
import com.gifisan.mtp.jms.Message;
import com.gifisan.mtp.server.MTPServlet;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;
import com.gifisan.mtp.server.session.Session;

public class JMSConsumerServlet extends MTPServlet{

//	private final Logger logger = LoggerFactory.getLogger(JMSConsumerServlet.class);
	
	public static String SERVICE_NAME = JMSConsumerServlet.class.getSimpleName();
	
	public void accept(Request request, Response response) throws Exception {

		
		Session session = request.getSession();
		
		MQContext context = MQContextFactory.getMQContext();
		
		if (context.isLogined(session)) {
			
//			long timeout = request.getLongParameter("timeout");
			
			context.pollMessage(request, response);
			
			
			/*
			if (session.connecting()) {

				if (message == null) {
					message = NullMessage.NULL_MESSAGE;
				}
				
				String content = message.toString();
				
				response.write(content.getBytes());
				
				response.flush();
				
//				logger.info("push message ,id ["+message.getMessageID()+"]");
				
			}else{
				if (message != null) {
					
					context.regist(message);
				}
			}
			*/
			
		}else{
			Message message = ErrorMessage.UNAUTH_MESSAGE;
			response.write(message.toString());
			response.flush();
			
		}
	}
}
