package com.gifisan.nio.service;

import java.io.IOException;

import com.gifisan.nio.Encoding;
import com.gifisan.nio.FlushedException;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.component.DynamicClassLoader;
import com.gifisan.nio.component.RESMessage;
import com.gifisan.nio.server.ServerContext;
import com.gifisan.nio.server.session.Session;
import com.gifisan.nio.service.impl.ErrorServlet;

public final class ServletFilter extends AbstractNIOFilter {

	private DynamicClassLoader	classLoader	= null;
	private Logger				logger		= LoggerFactory.getLogger(ServletFilter.class);
	private ServletLoader		servletLoader	= null;

	public ServletFilter(DynamicClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	private void accept(Exception exception, Session session) throws IOException {
		
		ErrorServlet servlet = new ErrorServlet(exception);
		
		try {
			
			servlet.accept(session);
			
		} catch (IOException e) {
			
			throw e;
			
		} catch (Exception e) {
			
			logger.error(e.getMessage(),e);
			
		}
	}

	public void accept(Session session) throws IOException {
		
		String serviceName = session.getServiceName();
		
		if (StringUtil.isNullOrBlank(serviceName)) {
			
			this.accept404(session);

		} else {
			
			this.accept(serviceName, session);
			
		}
	}

	private void accept(ServiceAcceptor servlet, Session session) throws IOException {
		try {
			servlet.accept(session);
		} catch (FlushedException e) {
			logger.error(e.getMessage(),e);
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			this.accept(e, session);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			this.accept(e, session);
		}

	}

	private void accept(String serviceName, Session session) throws IOException {
		
		ServiceAcceptor servlet = servletLoader.getServlet(serviceName);
		
		if (servlet == null) {
			
			this.accept404(session, serviceName);
			
		} else {
			
			this.accept(servlet, session);
		}
	}

	private void accept404(Session session) throws IOException {
		
		logger.info("[NIOServer] empty service name");
		
		session.write(RESMessage.R404_EMPTY.toString().getBytes(Encoding.DEFAULT));
		
		session.flush();
	}

	private void accept404(Session session, String serviceName) throws IOException {
		
		logger.info("[NIOServer] 未发现命令：" + serviceName);
		
		RESMessage message = new RESMessage(404, "service name not found :" + serviceName);
		
		session.write(message.toString());
		
		session.flush();
	}

	public void destroy(ServerContext context, Configuration config) throws Exception {
		LifeCycleUtil.stop(servletLoader);

	}

	public void initialize(ServerContext context, Configuration config) throws Exception {

		this.servletLoader = new NormalServletLoader(context,	classLoader);

		this.servletLoader.start();
	}

	public void prepare(ServerContext context, Configuration config) throws Exception {
		
		this.servletLoader = new NormalServletLoader(context, classLoader);
		
		this.servletLoader.prepare(context, config);
		
	}

	public void unload(ServerContext context, Configuration config) throws Exception {
		this.servletLoader.unload(context, config);
	}

}
