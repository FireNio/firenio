package com.gifisan.nio.jms.server;

import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.jms.ErrorMessage;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.server.session.IOSession;

public class JMSConsumerServlet extends JMSServlet{

	public void accept(IOSession session,ServerReadFuture future,JMSSessionAttachment attachment) throws Exception {
		
		MQContext context = getMQContext();
		
		if (context.isLogined(attachment)) {
			
			context.pollMessage(session, future, attachment);
			
		}else{
			Message message = ErrorMessage.UNAUTH_MESSAGE;
			future.write(message.toString());
			session.flush(future);
			
		}
	}
}
