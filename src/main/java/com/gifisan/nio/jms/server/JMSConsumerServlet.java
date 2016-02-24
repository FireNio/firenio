package com.gifisan.nio.jms.server;

import com.gifisan.nio.jms.ErrorMessage;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.server.Request;
import com.gifisan.nio.server.Response;
import com.gifisan.nio.server.session.Session;

public class JMSConsumerServlet extends JMSServlet{

	public void accept(Request request, Response response) throws Exception {
		
		Session session = request.getSession();
		
		MQContext context = getMQContext();
		
		if (context.isLogined(session)) {
			
			context.pollMessage(request, response);
			
		}else{
			Message message = ErrorMessage.UNAUTH_MESSAGE;
			response.write(message.toString());
			response.flush();
			
		}
	}
}
