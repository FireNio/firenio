package com.gifisan.mtp.jms.server;

import com.gifisan.mtp.jms.Message;
import com.gifisan.mtp.server.MTPServlet;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;
import com.gifisan.mtp.server.session.Session;

public class JMSProducerServlet extends MTPServlet{
	
//	private final Logger logger = LoggerFactory.getLogger(JMSProducerServlet.class);

	public static String SERVICE_NAME = JMSProducerServlet.class.getSimpleName();
	
	private final byte TRUE = 'T';
	
	private final byte FALSE = 'F';
	
	public void accept(Request request, Response response) throws Exception {
		
		Session session = request.getSession();
		
		MQContext context = MQContextFactory.getMQContext();
		
		if (context.isLogined(session)) {
			Message message = context.parse(request);
			
			byte result = context.offerMessage(message) ? TRUE : FALSE ;
			
			response.write(result);
			
		}else{
			
			response.write("用户未登录！");
				
		}

		response.flush();
		
	}

}
