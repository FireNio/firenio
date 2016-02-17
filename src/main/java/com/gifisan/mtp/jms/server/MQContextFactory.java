package com.gifisan.mtp.jms.server;

import java.util.concurrent.atomic.AtomicBoolean;

import com.gifisan.mtp.common.LifeCycleUtil;

public class MQContextFactory {

	private static MQContext		context		= new MQContextImpl();
	private static AtomicBoolean	initialized	= new AtomicBoolean(false);

	public static MQContext getMQContext() {
		return context;
	}

	public static void initializeContext() throws Exception {
		if (initialized.compareAndSet(false, true)) {
			context.start();
		}
	}

	public static void setNullMQContext() {
		LifeCycleUtil.stop(context);
		context = null;
		initialized = null;
	}

}
