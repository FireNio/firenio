package com.gifisan.nio.jms.server;

import java.io.OutputStream;

import com.gifisan.nio.component.BufferedOutputStream;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.server.session.Session;
import com.gifisan.nio.service.Request;
import com.gifisan.nio.service.Response;

public class JMSPublishServlet extends JMSServlet {

	private final byte	TRUE		= 'T';
	private final byte	FALSE	= 'F';

	public void accept(Request request, Response response, JMSSessionAttachment attachment) throws Exception {

		Session session = request.getSession();

		MQContext context = getMQContext();

		if (context.isLogined(session)) {
			
			if (session.isStream()) {
				
				OutputStream outputStream = session.getServerOutputStream();
				
				if (outputStream == null) {
					session.setServerOutputStream(new BufferedOutputStream());
					return;
				}
			}
			
			Message message = context.parse(request);

			byte result = context.publishMessage(message) ? TRUE : FALSE;

			response.write(result);

		} else {

			response.write("用户未登录！");

		}

		response.flush();

	}

}
