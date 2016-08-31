package com.generallycloud.nio.extend.plugin.jms.server;

import java.io.OutputStream;

import com.generallycloud.nio.common.ByteUtil;
import com.generallycloud.nio.component.BufferedOutputStream;
import com.generallycloud.nio.component.ChannelBufferOutputstream;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.protocol.nio.future.NIOReadFuture;
import com.generallycloud.nio.extend.plugin.jms.Message;

public class MQPublishServlet extends MQServlet {

	public static final String	SERVICE_NAME	= MQPublishServlet.class.getSimpleName();

	public void doAccept(Session session, NIOReadFuture future, MQSessionAttachment attachment) throws Exception {

		MQContext context = getMQContext();

		if (future.hasOutputStream()) {

			OutputStream outputStream = future.getOutputStream();

			if (outputStream == null) {
				future.setOutputStream(new ChannelBufferOutputstream());
				return;
			}
		}

		Message message = context.parse(future);

		context.publishMessage(message);

		future.write(ByteUtil.TRUE);

		session.flush(future);
	}
}
