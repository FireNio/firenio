package com.gifisan.nio.jms.server;

import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.jms.ErrorMessage;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.server.IOSession;

public class JMSSubscribeServlet extends JMSServlet{

	public void accept(IOSession session,ServerReadFuture future,JMSSessionAttachment attachment) throws Exception {
		
		if (attachment.getAuthority() != null && attachment.getAuthority().isAuthored()) {
			
			getMQContext().subscribeMessage(session, future, attachment);
			
		}else{
			Message message = ErrorMessage.UNAUTH_MESSAGE;
			future.write(message.toString());
			session.flush(future);
			
		}
	}
}
