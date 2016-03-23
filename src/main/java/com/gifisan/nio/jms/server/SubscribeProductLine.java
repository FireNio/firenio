package com.gifisan.nio.jms.server;


public class SubscribeProductLine extends P2PProductLine implements MessageQueue, Runnable {

	public SubscribeProductLine(MQContext context) {
		super(context);
	}

}
