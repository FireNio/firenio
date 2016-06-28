package com.gifisan.nio.extend.plugin.jms.server;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;

public class MQSubscribeServlet extends MQServlet {

	public static final String	SERVICE_NAME	= MQSubscribeServlet.class.getSimpleName();

	public void accept(Session session, ReadFuture future, MQSessionAttachment attachment) throws Exception {

		getMQContext().subscribeMessage(session, future, attachment);

	}
}
