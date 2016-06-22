package com.gifisan.nio.plugin.jms.server;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;

public class JMSConsumerServlet extends JMSServlet {

	public static final String	SERVICE_NAME	= JMSConsumerServlet.class.getSimpleName();

	public void accept(Session session, ReadFuture future, JMSSessionAttachment attachment) throws Exception {
		
		getMQContext().pollMessage(session, future, attachment);
	}
}
