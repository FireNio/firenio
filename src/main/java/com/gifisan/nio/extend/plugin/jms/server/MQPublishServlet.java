package com.gifisan.nio.extend.plugin.jms.server;

import java.io.OutputStream;

import com.gifisan.nio.common.ByteUtil;
import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.protocol.nio.future.NIOReadFuture;
import com.gifisan.nio.extend.plugin.jms.Message;

public class MQPublishServlet extends MQServlet {

	public static final String	SERVICE_NAME	= MQPublishServlet.class.getSimpleName();

	public void accept(Session session, NIOReadFuture future, MQSessionAttachment attachment) throws Exception {

		MQContext context = getMQContext();

		if (future.hasOutputStream()) {

			OutputStream outputStream = future.getOutputStream();

			if (outputStream == null) {
				future.setOutputStream(new BufferedOutputStream(future.getStreamLength()));
				return;
			}
		}

		Message message = context.parse(future);

		context.publishMessage(message);

		future.write(ByteUtil.TRUE);

		session.flush(future);
	}
}
