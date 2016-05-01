package com.gifisan.nio.jms.server;

import com.gifisan.nio.Encoding;
import com.gifisan.nio.common.ByteUtil;
import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.jms.ErrorMessage;
import com.gifisan.nio.jms.Message;
import com.gifisan.nio.jms.NullMessage;
import com.gifisan.nio.server.IOSession;

public class JMSBrowserServlet extends JMSServlet {
	
	public static String SIZE = "0";
	
	public static String BROWSER = "1";
	
	public static String ONLINE = "2";
	
	public void accept(IOSession session,ServerReadFuture future,JMSSessionAttachment attachment) throws Exception {

		Parameters param = future.getParameters();

		String messageID = param.getParameter("messageID");

		Message message = NullMessage.NULL_MESSAGE;

		if (attachment.getAuthority() != null && attachment.getAuthority().isAuthored()) {

			String cmd = param.getParameter("cmd");
			if (StringUtil.isNullOrBlank(cmd)) {
				message = ErrorMessage.CMD_NOT_FOUND_MESSAGE;
			} else {
				
				MQContext context = getMQContext();
				
				if (SIZE.equals(cmd)) {
					
					future.write(String.valueOf(context.messageSize()));
					
				} else if (BROWSER.equals(cmd)) {

					if (!StringUtil.isNullOrBlank(messageID)) {
						
						message = context.browser(messageID);

						if (message == null) {

							message = NullMessage.NULL_MESSAGE;

							future.write(message.toString());
						} else {
							
							future.write(message.toString(), Encoding.DEFAULT);
						}
					}
				}else if(ONLINE.equals(cmd)){
					
					boolean bool = context.isOnLine(param.getParameter("queueName"));
					
					byte result =  ByteUtil.getByte(bool);
					
					future.write(result);
				}
			}
		} else {
			message = ErrorMessage.UNAUTH_MESSAGE;
			future.write(message.toString());
		}

		session.flush(future);
	}


	
	
}
