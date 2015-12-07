package com.yoocent.mtp.jms.server;

import com.yoocent.mtp.jms.Message;
import com.yoocent.mtp.server.MTPServlet;
import com.yoocent.mtp.server.Request;
import com.yoocent.mtp.server.Response;
import com.yoocent.mtp.server.session.Session;

public class JMSProducerServlet extends MTPServlet{

	public static String SERVICE_NAME = JMSProducerServlet.class.getSimpleName();
	
	private static final byte TRUE = 'T';
	
	private static final byte FALSE = 'F';
	
	public void accept(Request request, Response response) throws Exception {
		
		Session session = request.getSession();
		
		MQContext context = MQContextFactory.getMQContext();
		
		if (context.isLogined(session)) {
			Message message = context.parse(request);
			
			byte result = context.regist(message) ? TRUE : FALSE ;
			
			response.write(result);
			
		}else{
			
			response.write("用户未登录！");
				
		}

		response.flush();
		
	}

}
