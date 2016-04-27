package com.gifisan.nio.jms.server;

import java.io.OutputStream;

import com.gifisan.nio.common.ByteUtil;
import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.server.session.IOSession;

public class JMSPublishServlet extends JMSServlet {

	public void accept(IOSession session,ServerReadFuture future,JMSSessionAttachment attachment) throws Exception {

		MQContext context = getMQContext();

		if (context.isLogined(attachment)) {
			
			if (future.hasOutputStream()) {
				
				OutputStream outputStream = future.getOutputStream();
				
				if (outputStream == null) {
					future.setOutputIOEvent(new BufferedOutputStream(future.getStreamLength()), null);
					return;
				}
			}
			
			Message message = context.parse(future);

			context.publishMessage(message);

			future.write(ByteUtil.TRUE);

		} else {

			future.write("用户未登录！");

		}

		session.flush(future);
	}
}
