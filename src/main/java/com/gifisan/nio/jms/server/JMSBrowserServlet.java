package com.gifisan.nio.jms.server;

import com.gifisan.nio.Encoding;
import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.jms.ErrorMessage;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.jms.NullMessage;
import com.gifisan.nio.server.session.Session;
import com.gifisan.nio.service.Request;
import com.gifisan.nio.service.Response;

public class JMSBrowserServlet extends JMSServlet {

	public void accept(Request request, Response response,JMSSessionAttachment attachment) throws Exception {

		Parameters param = request.getParameters();

		String messageID = param.getParameter("messageID");

		Session session = request.getSession();

		MQContext context = getMQContext();

		Message message = NullMessage.NULL_MESSAGE;

		if (context.isLogined(session)) {

			String cmd = param.getParameter("cmd");
			if (StringUtil.isNullOrBlank(cmd)) {
				message = ErrorMessage.CMD_NOT_FOUND_MESSAGE;
			} else {
				accept(context, response, message, messageID, cmd);
			}
		} else {
			message = ErrorMessage.UNAUTH_MESSAGE;
			response.write(message.toString());
		}

		response.flush();
	}

	private void accept(MQContext context, Response response, Message message, String messageID, String cmd) {

		if ("size".equals(cmd)) {
			response.write(String.valueOf(context.messageSize()));
		} else if ("browser".equals(cmd)) {

			if (!StringUtil.isNullOrBlank(messageID)) {
				message = context.browser(messageID);

				if (message == null) {

					message = NullMessage.NULL_MESSAGE;

					response.write(message.toString());
				} else {
					response.write(message.toString(), Encoding.DEFAULT);
				}
			}
		}
	}

}
