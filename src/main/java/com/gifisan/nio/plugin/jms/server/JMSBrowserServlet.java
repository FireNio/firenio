package com.gifisan.nio.plugin.jms.server;

import com.gifisan.nio.common.ByteUtil;
import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.component.ByteArrayInputStream;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.plugin.jms.ByteMessage;
import com.gifisan.nio.plugin.jms.ErrorMessage;
import com.gifisan.nio.plugin.jms.Message;
import com.gifisan.nio.plugin.jms.NullMessage;
import com.gifisan.nio.server.IOSession;

public class JMSBrowserServlet extends JMSServlet {

	public static final String	SIZE			= "0";

	public static final String	BROWSER		= "1";

	public static final String	ONLINE		= "2";

	public static final String	SERVICE_NAME	= JMSBrowserServlet.class.getSimpleName();

	public void accept(IOSession session, ServerReadFuture future, JMSSessionAttachment attachment) throws Exception {

		Parameters param = future.getParameters();

		String messageID = param.getParameter("messageID");

		Message message = NullMessage.NULL_MESSAGE;

		MQContext context = getMQContext();

		String cmd = param.getParameter("cmd");
		if (StringUtil.isNullOrBlank(cmd)) {
			message = ErrorMessage.CMD_NOT_FOUND_MESSAGE;
		} else {

			if (SIZE.equals(cmd)) {

				future.write(String.valueOf(context.messageSize()));

			} else if (BROWSER.equals(cmd)) {

				if (!StringUtil.isNullOrBlank(messageID)) {

					message = context.browser(messageID);

					if (message == null) {

						message = NullMessage.NULL_MESSAGE;

						future.write(message.toString());
					} else {

						int msgType = message.getMsgType();

						String content = message.toString();

						future.write(content);

						if (msgType == 3) {

							ByteMessage byteMessage = (ByteMessage) message;

							byte[] bytes = byteMessage.getByteArray();

							future.setInputIOEvent(new ByteArrayInputStream(bytes), null);
						}
					}
				}
			} else if (ONLINE.equals(cmd)) {

				boolean bool = context.isOnLine(param.getParameter("queueName"));

				byte result = ByteUtil.getByte(bool);

				future.write(result);
			}
		}

		session.flush(future);
	}

}
