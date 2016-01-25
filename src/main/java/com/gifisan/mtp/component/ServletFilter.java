package com.gifisan.mtp.component;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gifisan.mtp.Encoding;
import com.gifisan.mtp.common.LifeCycleUtil;
import com.gifisan.mtp.common.SharedBundle;
import com.gifisan.mtp.common.StringUtil;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;
import com.gifisan.mtp.server.ServerContext;
import com.gifisan.mtp.server.ServletAcceptor;
import com.gifisan.mtp.servlet.DebugServletLoader;
import com.gifisan.mtp.servlet.MTPFilter;
import com.gifisan.mtp.servlet.NormalServletLoader;
import com.gifisan.mtp.servlet.ServletLoader;
import com.gifisan.mtp.servlet.impl.ErrorServlet;

public final class ServletFilter implements MTPFilter {

	public void initialize(ServerContext context, FilterConfig config) throws Exception {

		boolean debug = SharedBundle.instance().getBooleanProperty("SERVER.DEBUG");

		if (debug) {
			this.servletLoader = new DebugServletLoader(context,classLoader);
		} else {
			this.classLoader.scan(context.getAppLocalAddress());
			this.servletLoader = new NormalServletLoader(context, classLoader);
		}

		this.servletLoader.start();
	}

	public void destroy(ServerContext context, FilterConfig config) throws Exception {
		LifeCycleUtil.stop(servletLoader);

	}

	private ServerContext		context		= null;
	private final Logger		logger		= LoggerFactory.getLogger(ServletFilter.class);
	private ServletLoader		servletLoader	= null;
	private DynamicClassLoader	classLoader	= new DynamicClassLoader();

	public void accept(Request request, Response response) throws IOException {
		String serviceName = request.getServiceName();
		if (StringUtil.isNullOrBlank(serviceName)) {
			this.accept404(request, response);

		} else {
			this.acceptNormal(serviceName, request, response);
		}
	}

	private void accept404(Request request, Response response) throws IOException {
		logger.info("[MTPServer] empty service name");
		response.write(RESMessage.R404_EMPTY.toString().getBytes(Encoding.DEFAULT));
		response.flush();
	}

	private void accept404(Request request, Response response, String serviceName) throws IOException {
		logger.info("[MTPServer] 未发现命令：" + serviceName);
		RESMessage message = new RESMessage(404, "service name not found :" + serviceName);
		response.write(message.toString());
		response.flush();
	}

	private void acceptNormal(String serviceName, Request request, Response response) throws IOException {
		ServletAcceptor servlet = servletLoader.getServlet(serviceName);
		if (servlet == null) {
			this.accept404(request, response, serviceName);
		} else {
			this.acceptNormal0(servlet, request, response);
		}
	}

	private void acceptNormal0(ServletAcceptor servlet, Request request, Response response) throws IOException {
		try {
			servlet.accept(request, response);
		} catch (FlushedException e) {
			e.printStackTrace();
		} catch (MTPChannelException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			this.acceptException(e, request, response);
		} catch (Exception e) {
			e.printStackTrace();
			this.acceptException(e, request, response);
		}

	}

	public boolean redeploy() {

		DynamicClassLoader _classLoader = new DynamicClassLoader();

		try {
			_classLoader.scan(context.getAppLocalAddress());
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		if (servletLoader.redeploy(_classLoader)) {
			this.classLoader = _classLoader;
			return true;
		}
		return false;

	}

	private void acceptException(Exception exception, Request request, Response response) throws IOException {
		ErrorServlet servlet = new ErrorServlet(exception);
		try {
			servlet.accept(request, response);
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
