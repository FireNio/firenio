package com.gifisan.nio.jms.server;

import com.gifisan.nio.jms.ErrorMessage;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.server.session.ServerSession;

public class JMSConsumerServlet extends JMSServlet{

	public void accept(ServerSession session,JMSSessionAttachment attachment) throws Exception {
		
		MQContext context = getMQContext();
		
		if (context.isLogined(session)) {
			
			context.pollMessage(session,attachment);
			
		}else{
			Message message = ErrorMessage.UNAUTH_MESSAGE;
			session.write(message.toString());
			session.flush();
			
		}
	}
}
