package com.yoocent.mtp.jms.server;

public class MQContextFactory {

	private static MQContext context = new MQContextImpl();
	
	public static MQContext getMQContext(){
		
		return context;
	}
	
}
