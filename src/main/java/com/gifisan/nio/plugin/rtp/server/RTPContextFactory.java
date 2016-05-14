package com.gifisan.nio.plugin.rtp.server;

import java.util.concurrent.atomic.AtomicBoolean;

import com.gifisan.nio.server.ServerContext;

public class RTPContextFactory {

	private static RTPContext	context		= null;
	private static AtomicBoolean	initialized	= new AtomicBoolean(false);

	public static RTPContext getRTPContext() {
		return context;
	}

	public static void initializeContext(ServerContext context) throws Exception {
		if (initialized.compareAndSet(false, true)) {

			RTPContextFactory.context = (RTPContext) context.getPluginContext(RTPContext.class);
		}
	}

	public static void setNullRTPContext() {
		context = null;
		initialized.set(false);
	}

}
