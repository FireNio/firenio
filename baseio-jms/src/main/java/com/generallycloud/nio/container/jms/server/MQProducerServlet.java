package com.generallycloud.nio.container.jms.server;

import com.generallycloud.nio.codec.protobase.future.ProtobaseReadFuture;
import com.generallycloud.nio.common.ByteUtil;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.jms.Message;

public class MQProducerServlet extends MQServlet {

	public static final String	SERVICE_NAME	= MQProducerServlet.class.getSimpleName();

	public void doAccept(SocketSession session, ProtobaseReadFuture future, MQSessionAttachment attachment) throws Exception {

		MQContext context = getMQContext();

		Message message = context.parse(future);

		context.offerMessage(message);

		future.write(ByteUtil.TRUE);

		session.flush(future);

	}

}
