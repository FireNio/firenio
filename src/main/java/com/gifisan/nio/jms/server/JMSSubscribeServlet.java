package com.gifisan.nio.jms.server;

import com.gifisan.nio.component.ReadFuture;
import com.gifisan.nio.jms.ErrorMessage;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.server.session.IOSession;

public class JMSSubscribeServlet extends JMSServlet{

	public void accept(IOSession session,ReadFuture future,JMSSessionAttachment attachment) throws Exception {
		
		MQContext context = getMQContext();
		
		if (context.isLogined(attachment)) {
			
			context.subscribeMessage(session, future, attachment);
			
		}else{
			Message message = ErrorMessage.UNAUTH_MESSAGE;
			session.write(message.toString());
			session.flush(future);
			
		}
	}
}
