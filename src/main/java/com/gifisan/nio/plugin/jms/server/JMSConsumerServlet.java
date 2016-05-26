package com.gifisan.nio.plugin.jms.server;

import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.IOSession;

public class JMSConsumerServlet extends JMSServlet {

	public static final String	SERVICE_NAME	= JMSConsumerServlet.class.getSimpleName();

	public void accept(IOSession session, ServerReadFuture future, JMSSessionAttachment attachment) throws Exception {
		
		getMQContext().pollMessage(session, future, attachment);
	}
}
