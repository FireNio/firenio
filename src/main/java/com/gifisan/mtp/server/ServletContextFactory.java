package com.gifisan.mtp.server;

import com.gifisan.mtp.AbstractLifeCycle;
import com.gifisan.mtp.LifeCycle;
import com.gifisan.mtp.common.LifeCycleUtil;
import com.gifisan.mtp.server.context.ServletContext;
import com.gifisan.mtp.servlet.ServletContextImpl;

public class ServletContextFactory extends AbstractLifeCycle implements LifeCycle{

	public ServletContextFactory (MTPServer server){
		ContextHolder.server = server;
	}
	
	protected void doStart() throws Exception {
		ContextHolder.initialize();
		factory = this;
	}

	protected void doStop() throws Exception {
		ContextHolder.unpackContext();
		factory = null;
	}
	
	private static ServletContextFactory factory = null;
	
	protected static ServletContext getServletContext() {
		if (factory != null && factory.isRunning()) {
			return ContextHolder.context;
		}
		return null;
	}

	private static class ContextHolder {
		
		private static MTPServer server = null;
		
		private static ServletContext context = null;
		
		static void initialize() throws Exception{
			context = new ServletContextImpl(server);
			context.start();
		}
		
		static void unpackContext() throws Exception{
			LifeCycleUtil.stop(context);
		}
	}
	
}
