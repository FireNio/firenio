package com.gifisan.nio.jms.server;

import com.gifisan.nio.server.NIOServlet;

public abstract class JMSServlet extends NIOServlet{

	private MQContext context = MQContextFactory.getMQContext();

	public MQContext getMQContext() {
		return context;
	}
	
}
