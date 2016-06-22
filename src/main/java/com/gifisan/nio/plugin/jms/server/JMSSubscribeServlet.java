package com.gifisan.nio.plugin.jms.server;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;

public class JMSSubscribeServlet extends JMSServlet {

	public static final String	SERVICE_NAME	= JMSSubscribeServlet.class.getSimpleName();

	public void accept(Session session, ReadFuture future, JMSSessionAttachment attachment) throws Exception {

		getMQContext().subscribeMessage(session, future, attachment);

	}
}
