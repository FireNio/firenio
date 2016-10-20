package com.generallycloud.nio.extend.plugin.jms.server;

import com.generallycloud.nio.codec.nio.future.NIOReadFuture;
import com.generallycloud.nio.component.Session;

public class MQSubscribeServlet extends MQServlet {

	public static final String	SERVICE_NAME	= MQSubscribeServlet.class.getSimpleName();

	public void doAccept(Session session, NIOReadFuture future, MQSessionAttachment attachment) throws Exception {

		getMQContext().subscribeMessage(session, future, attachment);

	}
}
