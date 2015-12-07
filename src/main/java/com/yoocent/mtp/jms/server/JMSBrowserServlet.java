package com.yoocent.mtp.jms.server;

import com.yoocent.mtp.common.StringUtil;
import com.yoocent.mtp.jms.ErrorMessage;
import com.yoocent.mtp.jms.Message;
import com.yoocent.mtp.jms.NullMessage;
import com.yoocent.mtp.server.MTPServlet;
import com.yoocent.mtp.server.Request;
import com.yoocent.mtp.server.Response;
import com.yoocent.mtp.server.session.Session;

public class JMSBrowserServlet extends MTPServlet{

	public static String SERVICE_NAME = JMSBrowserServlet.class.getSimpleName();
	
	public void accept(Request request, Response response) throws Exception {
		
		
		String messageID = request.getStringParameter("messageID");
		
		Session session = request.getSession();
		
		MQContext context = MQContextFactory.getMQContext();
		
		Message message = NullMessage.NULL_MESSAGE;
		
		if (context.isLogined(session)) {
		
			if (!StringUtil.isBlankOrNull(messageID)) {
				message = context.browser(messageID);
				
				if (message == null) {
					
					message = NullMessage.NULL_MESSAGE;
					
					response.write(message.toString());
				}else {
					response.write(message.toString(), "UTF-8");
				}
			}
			
		}else{
			message = ErrorMessage.UNAUTH_MESSAGE;
		}

		response.flush();
	}

	
	

}
