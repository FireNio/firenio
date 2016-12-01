package com.generallycloud.nio.container.jms.server;

import com.generallycloud.nio.codec.base.future.BaseReadFuture;
import com.generallycloud.nio.component.SocketSession;

public class MQConsumerServlet extends MQServlet {

	public static final String	SERVICE_NAME	= MQConsumerServlet.class.getSimpleName();

	public void doAccept(SocketSession session, BaseReadFuture future, MQSessionAttachment attachment) throws Exception {
		
		getMQContext().pollMessage(session, future, attachment);
	}
}
