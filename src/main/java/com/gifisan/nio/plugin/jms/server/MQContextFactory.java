package com.gifisan.nio.plugin.jms.server;

import java.util.concurrent.atomic.AtomicBoolean;

public class MQContextFactory {

	private static MQContext		context		= null;
	private static AtomicBoolean	initialized	= new AtomicBoolean(false);

	public static MQContext getMQContext() {
		return context;
	}

	public static void initializeContext(MQContext context) throws Exception {
		if (initialized.compareAndSet(false, true)) {

			MQContextFactory.context = context;
		}
	}

	public static void setNullMQContext() {
		context = null;
		initialized.set(false);
	}

}
