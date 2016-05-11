package com.gifisan.nio.rtp.server;

import java.util.concurrent.atomic.AtomicBoolean;

import com.gifisan.nio.common.LifeCycleUtil;

public class RTPContextFactory {

	private static RTPContext	context		= new RTPContext();
	private static AtomicBoolean	initialized	= new AtomicBoolean(false);

	public static RTPContext getMQContext() {
		return context;
	}

	public static void initializeContext() throws Exception {
		if (initialized.compareAndSet(false, true)) {
			context.start();
		}
	}

	public static void setNullRTPContext() {
		LifeCycleUtil.stop(context);
		context = null;
		initialized = null;
	}

}
