package com.gifisan.mtp.jms.server;

import com.gifisan.mtp.Encoding;
import com.gifisan.mtp.common.StringUtil;
import com.gifisan.mtp.component.RequestParam;
import com.gifisan.mtp.jms.ErrorMessage;
import com.gifisan.mtp.jms.Message;
import com.gifisan.mtp.jms.NullMessage;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;
import com.gifisan.mtp.server.session.Session;

public class JMSBrowserServlet extends JMSServlet {

	public void accept(Request request, Response response) throws Exception {

		RequestParam param = request.getParameters();

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
			int size = context.messageSize();
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
