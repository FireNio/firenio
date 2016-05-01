package com.gifisan.nio.jms.server;

import java.io.OutputStream;

import com.gifisan.nio.common.ByteUtil;
import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.server.IOSession;

public class JMSProducerServlet extends JMSServlet {

	public void accept(IOSession session,ServerReadFuture future,JMSSessionAttachment attachment) throws Exception {

		if (attachment.getAuthority() != null && attachment.getAuthority().isAuthored()) {
			
			if (future.hasOutputStream()) {
				
				OutputStream outputStream = future.getOutputStream();
				
				if (outputStream == null) {
					future.setOutputIOEvent(new BufferedOutputStream(future.getStreamLength()), null);
					return;
				}
			}

			MQContext context = getMQContext();
			
			Message message = context.parse(future);
			
			context.offerMessage(message);

			future.write(ByteUtil.TRUE);

		} else {

			future.write("用户未登录！");

		}

		session.flush(future);

	}

}
