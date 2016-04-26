package com.gifisan.nio.jms.server;

import java.io.OutputStream;

import com.gifisan.nio.common.ByteUtil;
import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.component.ReadFuture;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.server.session.IOSession;

public class JMSProducerServlet extends JMSServlet {

	public void accept(IOSession session,ReadFuture future,JMSSessionAttachment attachment) throws Exception {

		MQContext context = getMQContext();

		if (context.isLogined(attachment)) {
			
			if (future.hasOutputStream()) {
				
				OutputStream outputStream = future.getOutputStream();
				
				if (outputStream == null) {
					future.setIOEvent(new BufferedOutputStream(),null);
					return;
				}
			}
			
			Message message = context.parse(future);
			
			context.offerMessage(message);

			session.write(ByteUtil.TRUE);

		} else {

			session.write("用户未登录！");

		}

		session.flush(future);

	}

}
