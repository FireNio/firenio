package com.gifisan.nio.jms.server;

import com.gifisan.nio.service.NIOServlet;
import com.gifisan.nio.service.Request;
import com.gifisan.nio.service.Response;

public abstract class JMSServlet extends NIOServlet{

	private MQContext context = MQContextFactory.getMQContext();

	public MQContext getMQContext() {
		return context;
	}

	public void accept(Request request, Response response) throws Exception {
		this.accept(request, response, (JMSSessionAttachment) request.getSession().attachment());
	}
	
	public abstract void accept(Request request, Response response,JMSSessionAttachment attachment) throws Exception;
	
}
