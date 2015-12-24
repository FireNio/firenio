package com.gifisan.mtp.jms.server;

import com.gifisan.mtp.common.StringUtil;
import com.gifisan.mtp.jms.ErrorMessage;
import com.gifisan.mtp.jms.Message;
import com.gifisan.mtp.jms.NullMessage;
import com.gifisan.mtp.server.MTPServlet;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;
import com.gifisan.mtp.server.session.Session;

public class JMSBrowserServlet extends MTPServlet{

	public static String SERVICE_NAME = JMSBrowserServlet.class.getSimpleName();
	
	public void accept(Request request, Response response) throws Exception {
		
		String messageID = request.getParameter("messageID");
		
		Session session = request.getSession();
		
		MQContext context = MQContextFactory.getMQContext();
		
		Message message = NullMessage.NULL_MESSAGE;
		
		if (context.isLogined(session)) {
		
			String cmd = request.getParameter("cmd");
			if (StringUtil.isNullOrBlank(cmd)) {
				message = ErrorMessage.CMD_NOT_FOUND_MESSAGE;
			}else{
				if ("size".equals(cmd)) {
					int size = context.messageSize();
				}else if("browser".equals(cmd)){
					
					if (!StringUtil.isNullOrBlank(messageID)) {
						message = context.browser(messageID);
						
						if (message == null) {
							
							message = NullMessage.NULL_MESSAGE;
							
							response.write(message.toString());
						}else {
							response.write(message.toString(), "UTF-8");
						}
					}
				}
				
			}
			
		}else{
			message = ErrorMessage.UNAUTH_MESSAGE;
		}

		response.flush();
	}

	
	

}
