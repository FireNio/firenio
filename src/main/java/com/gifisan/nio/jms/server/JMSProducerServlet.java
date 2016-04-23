package com.gifisan.nio.jms.server;

import java.io.OutputStream;

import com.gifisan.nio.common.ByteUtil;
import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.server.session.ServerSession;

public class JMSProducerServlet extends JMSServlet {

	public void accept(ServerSession session, JMSSessionAttachment attachment) throws Exception {

		MQContext context = getMQContext();

		if (context.isLogined(session)) {
			
			if (session.isStream()) {
				
				OutputStream outputStream = session.getServerOutputStream();
				
				if (outputStream == null) {
					session.setServerOutputStream(new BufferedOutputStream());
					return;
				}
			}
			
			Message message = context.parse(session);
			
			context.offerMessage(message);

			session.write(ByteUtil.TRUE);

		} else {

			session.write("用户未登录！");

		}

		session.flush();

	}

}
