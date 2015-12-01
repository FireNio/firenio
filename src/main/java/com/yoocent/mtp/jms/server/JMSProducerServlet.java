package com.yoocent.mtp.jms;

import com.yoocent.mtp.server.MTPServlet;
import com.yoocent.mtp.server.Request;
import com.yoocent.mtp.server.Response;

public class JMSProducerServlet extends MTPServlet{

	public static String SERVICE_KEY = JMSProducerServlet.class.getSimpleName();
	
	private static byte TRUE = 'T';
	
	private static byte FALSE = 'F';
	
	public void accept(Request request, Response response) throws Exception {

		JMSMessage message = JMSMessage.newMessage(request);
		
		message.setContent(request.getStringParameter("content"));
		
		byte result = JMSUtil.reg(message) ? TRUE : FALSE;
		
		response.write(result);
		
		response.flush();
		
	}

}
