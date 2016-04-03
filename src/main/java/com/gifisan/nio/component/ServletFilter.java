package com.gifisan.nio.component;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gifisan.nio.Encoding;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.server.Request;
import com.gifisan.nio.server.Response;
import com.gifisan.nio.server.ServerContext;
import com.gifisan.nio.server.ServiceAcceptor;
import com.gifisan.nio.servlet.AbstractNIOFilter;
import com.gifisan.nio.servlet.NormalServletLoader;
import com.gifisan.nio.servlet.ServletLoader;
import com.gifisan.nio.servlet.impl.ErrorServlet;

public final class ServletFilter extends AbstractNIOFilter {

	private DynamicClassLoader	classLoader	= null;
	private Logger				logger		= LoggerFactory.getLogger(ServletFilter.class);
	private ServletLoader		servletLoader	= null;

	public ServletFilter(DynamicClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	private void accept(Exception exception, Request request, Response response) throws IOException {
		
		ErrorServlet servlet = new ErrorServlet(exception);
		
		try {
			
			servlet.accept(request, response);
			
		} catch (IOException e) {
			
			throw e;
			
		} catch (Exception e) {
			
			logger.error(e.getMessage(),e);
			
		}
	}

	public void accept(Request request, Response response) throws IOException {
		
		String serviceName = request.getServiceName();
		
		if (StringUtil.isNullOrBlank(serviceName)) {
			
			this.accept404(request, response);

		} else {
			
			this.accept(serviceName, request, response);
			
		}
	}

	private void accept(ServiceAcceptor servlet, Request request, Response response) throws IOException {
		try {
			servlet.accept(request, response);
		} catch (FlushedException e) {
			logger.error(e.getMessage(),e);
		} catch (IOException e) {
			logger.error(e.getMessage(),e);
			this.accept(e, request, response);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			this.accept(e, request, response);
		}

	}

	private void accept(String serviceName, Request request, Response response) throws IOException {
		
		ServiceAcceptor servlet = servletLoader.getServlet(serviceName);
		
		if (servlet == null) {
			
			this.accept404(request, response, serviceName);
			
		} else {
			
			this.accept(servlet, request, response);
		}
	}

	private void accept404(Request request, Response response) throws IOException {
		
		logger.info("[NIOServer] empty service name");
		
		response.write(RESMessage.R404_EMPTY.toString().getBytes(Encoding.DEFAULT));
		
		response.flush();
	}

	private void accept404(Request request, Response response, String serviceName) throws IOException {
		
		logger.info("[NIOServer] 未发现命令：" + serviceName);
		
		RESMessage message = new RESMessage(404, "service name not found :" + serviceName);
		
		response.write(message.toString());
		
		response.flush();
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
