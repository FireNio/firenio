package com.gifisan.nio.plugin.jms.server;

import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.plugin.jms.ErrorMessage;
import com.gifisan.nio.plugin.jms.Message;
import com.gifisan.nio.server.IOSession;

public class JMSConsumerServlet extends JMSServlet{

	public static final String SERVICE_NAME = JMSConsumerServlet.class.getSimpleName();
	
	public void accept(IOSession session,ServerReadFuture future,JMSSessionAttachment attachment) throws Exception {
		
		MQContext context = getMQContext();

		if (context.isLogined(session)) {
			
			getMQContext().pollMessage(session, future, attachment);
			
		}else{
			Message message = ErrorMessage.UNAUTH_MESSAGE;
			future.write(message.toString());
			session.flush(future);
			
		}
	}
}
