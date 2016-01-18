package com.gifisan.mtp.component;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gifisan.mtp.AbstractLifeCycle;
import com.gifisan.mtp.Encoding;
import com.gifisan.mtp.LifeCycle;
import com.gifisan.mtp.common.LifeCycleUtil;
import com.gifisan.mtp.common.StringUtil;
import com.gifisan.mtp.server.Request;
import com.gifisan.mtp.server.Response;
import com.gifisan.mtp.server.ServletAcceptor;
import com.gifisan.mtp.server.ServletContext;
import com.gifisan.mtp.servlet.MTPFilterService;
import com.gifisan.mtp.servlet.MTPFilterServiceImpl;
import com.gifisan.mtp.servlet.impl.ErrorServlet;

public final class ServletService extends AbstractLifeCycle implements ServletAcceptor, LifeCycle {

	private ServletContext	context		= null;
	private MTPFilterService	filterService	= null;
	private final Logger	logger		= LoggerFactory.getLogger(ServletService.class);
	private ServletLoader	servletLoader	= new ServletLoader();

	public ServletService(ServletContext context) {
		this.context = context;
	}

	public void accept(Request request, Response response) throws IOException {
		try {
			if (filterService.doFilter(request, response)) {
				return;
			}
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

		String serviceName = request.getServiceName();
		this.accept(request, response, serviceName);
	}

	private void accept(Request request, Response response, String serviceName) throws IOException {
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

	protected void doStart() throws Exception {
		this.servletLoader.loadServlets(context);
		this.filterService = new MTPFilterServiceImpl(context);
		this.filterService.start();
		this.servletLoader.initialize();

	}

	protected void doStop() throws Exception {
		LifeCycleUtil.stop(filterService);
		servletLoader.unloadServlets();
	}

}
