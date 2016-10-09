package com.generallycloud.nio.extend.plugin.jms.server;

import com.generallycloud.nio.codec.nio.future.NIOReadFuture;
import com.generallycloud.nio.common.ByteUtil;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.extend.plugin.jms.Message;

public class MQProducerServlet extends MQServlet {

	public static final String	SERVICE_NAME	= MQProducerServlet.class.getSimpleName();

	public void doAccept(Session session, NIOReadFuture future, MQSessionAttachment attachment) throws Exception {

		MQContext context = getMQContext();

		Message message = context.parse(future);

		context.offerMessage(message);

		future.write(ByteUtil.TRUE);

		session.flush(future);

	}

}
