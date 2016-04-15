package com.gifisan.nio.jms.server;

import com.gifisan.nio.jms.ErrorMessage;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.server.session.Session;
import com.gifisan.nio.service.Request;
import com.gifisan.nio.service.Response;

public class JMSSubscribeServlet extends JMSServlet{

	public void accept(Request request, Response response,JMSSessionAttachment attachment) throws Exception {
		
		Session session = request.getSession();
		
		MQContext context = getMQContext();
		
		if (context.isLogined(session)) {
			
			context.subscribeMessage(request, response, attachment);
			
		}else{
			Message message = ErrorMessage.UNAUTH_MESSAGE;
			response.write(message.toString());
			response.flush();
			
		}
	}
}
