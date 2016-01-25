package com.gifisan.mtp.jms.server;

import com.gifisan.mtp.server.MTPServlet;

public abstract class JMSServlet extends MTPServlet{

	private MQContext context = MQContextFactory.getMQContext();

	public MQContext getMQContext() {
		return context;
	}
	
}
