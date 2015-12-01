package com.yoocent.mtp.jms.server;

import com.yoocent.mtp.jms.MessageParser;
import com.yoocent.mtp.jms.client.Message;
import com.yoocent.mtp.server.MTPServlet;
import com.yoocent.mtp.server.Request;
import com.yoocent.mtp.server.Response;

public class JMSProducerServlet extends MTPServlet{

	public static String SERVICE_KEY = JMSProducerServlet.class.getSimpleName();
	
	private static byte TRUE = 'T';
	
	private static byte FALSE = 'F';
	
	public void accept(Request request, Response response) throws Exception {
		
		String username = (String)request.getSession().getAttribute("username");
		
		if (username != null) {
			
			
			Message message = MessageParser.parse(request);
			
			
			byte result = JMSUtil.reg(message) ? TRUE : FALSE;
			
			response.write(result);
			
		}else{
			response.write("用户未登录！");
			
		}

		response.flush();
		
	}

}
