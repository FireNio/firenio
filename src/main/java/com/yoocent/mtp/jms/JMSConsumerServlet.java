package com.yoocent.mtp.jms;

import com.yoocent.mtp.server.MTPServlet;
import com.yoocent.mtp.server.Request;
import com.yoocent.mtp.server.Response;

public class JMSConsumerServlet extends MTPServlet{

	public static String SERVICE_KEY = JMSConsumerServlet.class.getSimpleName();
	
	public void accept(Request request, Response response) throws Exception {

		long timeout = request.getLongParameter("timeout");
		
		JMSMessage message = JMSUtil.pollMessage(request,timeout);
		
		String content = message.getContent();
		
		response.write(content.getBytes());
		
		response.flush();
		
	}

}
