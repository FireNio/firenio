package com.yoocent.mtp.jms.server;

import com.yoocent.mtp.jms.client.Message;
import com.yoocent.mtp.server.MTPServlet;
import com.yoocent.mtp.server.Request;
import com.yoocent.mtp.server.Response;
import com.yoocent.mtp.server.session.Session;

public class JMSConsumerServlet extends MTPServlet{

	public static String SERVICE_KEY = JMSConsumerServlet.class.getSimpleName();
	
	private static byte FALSE = 'F';
	
	public void accept(Request request, Response response) throws Exception {

		long timeout = request.getLongParameter("timeout");
		
		Message message = JMSUtil.pollMessage(request,timeout);
		
		
		Session session = request.getSession();
		
		if (session.connecting()) {

			if (message == null) {
				response.write(FALSE);
			}else{
				String content = message.toString();
				
				response.write(content.getBytes());
			}
			
			response.flush();
		}else{
			if (message != null) {
				
				JMSUtil.reg(message);
			}
			
		}
		
		
		
	}

}
