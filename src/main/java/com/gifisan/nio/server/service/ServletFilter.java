package com.gifisan.nio.server.service;

import java.io.IOException;

import com.gifisan.nio.Encoding;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.component.ApplicationContext;
import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.component.DynamicClassLoader;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.server.FilterAcceptor;
import com.gifisan.nio.server.RESMessage;

public final class ServletFilter extends AbstractNIOFilter {

	private DynamicClassLoader	classLoader	= null;
	private Logger				logger		= LoggerFactory.getLogger(ServletFilter.class);
	private ServletLoader		servletLoader	= null;

	public ServletFilter(DynamicClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public void accept(Session session,ReadFuture future) throws Exception {
		
		String serviceName = future.getServiceName();
		
		if (StringUtil.isNullOrBlank(serviceName)) {
			
			this.accept404(session,future);

		} else {
			
			this.accept(serviceName, session,future);
			
		}
	}

	private void accept(String serviceName, Session session,ReadFuture future) throws Exception {
		
		FilterAcceptor servlet = servletLoader.getServlet(serviceName);
		
		if (servlet == null) {
			
			this.accept404(session,future, serviceName);
			
		} else {
			
			servlet.accept(session,future);
		}
	}

	private void accept404(Session session,ReadFuture future) throws IOException {
		
		logger.info("[NIOServer] empty service name");
		
		future.write(RESMessage.EMPTY_404.toString().getBytes(Encoding.DEFAULT));
		
		session.flush(future);
	}

	private void accept404(Session session,ReadFuture future, String serviceName) throws IOException {
		
		logger.info("[NIOServer] 未发现命令：" + serviceName);
		
		RESMessage message = new RESMessage(404, "service name not found :" + serviceName);
		
		future.write(message.toString());
		
		session.flush(future);
	}

	public void destroy(ApplicationContext context, Configuration config) throws Exception {
		LifeCycleUtil.stop(servletLoader);

	}

	public void initialize(ApplicationContext context, Configuration config) throws Exception {

		this.servletLoader = new NormalServletLoader(context,	classLoader);

		this.servletLoader.start();
	}

	public void prepare(ApplicationContext context, Configuration config) throws Exception {
		
		this.servletLoader = new NormalServletLoader(context, classLoader);
		
		this.servletLoader.prepare(context, config);
		
	}

	public void unload(ApplicationContext context, Configuration config) throws Exception {
		this.servletLoader.unload(context, config);
	}

}
