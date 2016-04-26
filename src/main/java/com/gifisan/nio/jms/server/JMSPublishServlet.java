package com.gifisan.nio.jms.server;

import java.io.OutputStream;

import com.gifisan.nio.common.ByteUtil;
import com.gifisan.nio.component.ReadFuture;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.server.session.IOSession;

public class JMSPublishServlet extends JMSServlet {

	public void accept(IOSession session,ReadFuture future,JMSSessionAttachment attachment) throws Exception {

		MQContext context = getMQContext();

		if (context.isLogined(attachment)) {
			
			if (future.hasOutputStream()) {
				
				OutputStream outputStream = future.getOutputStream();
				
				if (outputStream == null) {
					future.setIOEvent(outputStream, null);
					return;
				}
			}
			
			Message message = context.parse(future);

			context.publishMessage(message);

			session.write(ByteUtil.TRUE);

		} else {

			session.write("用户未登录！");

		}

		session.flush(future);
	}
}
