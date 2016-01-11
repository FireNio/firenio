package com.gifisan.mtp.jms.server;

import com.gifisan.mtp.jms.ErrorMessage;
import com.gifisan.mtp.jms.Message;
import com.gifisan.mtp.server.MTPServlet;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;
import com.gifisan.mtp.server.session.Session;

public class JMSConsumerServlet extends MTPServlet{

	public static String SERVICE_NAME = JMSConsumerServlet.class.getSimpleName();
	
	public void accept(Request request, Response response) throws Exception {
		
		Session session = request.getSession();
		
		MQContext context = MQContextFactory.getMQContext();
		
		if (context.isLogined(session)) {
			
			Consumer consumer = (Consumer) session.getAttribute("_consumer");
			
			if (consumer == null) {
				context.pollMessage(request, response);
			}else{
				consumer.update(request, response);
			}
			
		}else{
			Message message = ErrorMessage.UNAUTH_MESSAGE;
			response.write(message.toString());
			response.flush();
			
		}
	}
}
