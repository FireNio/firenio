package com.gifisan.nio.component;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.server.InnerResponse;
import com.gifisan.nio.server.Request;
import com.gifisan.nio.server.Response;
import com.gifisan.nio.server.ServerContext;
import com.gifisan.nio.server.ServiceAcceptor;
import com.gifisan.nio.servlet.FilterLoader;
import com.gifisan.nio.servlet.NIOFilterWrapper;
import com.gifisan.nio.servlet.NormalFilterLoader;
import com.gifisan.nio.servlet.impl.ErrorServlet;

public final class FilterService extends AbstractLifeCycle implements ServiceAcceptor, LifeCycle {

	private Logger				logger		= LoggerFactory.getLogger(FilterService.class);
	private ServerContext		context		= null;
	private NIOFilterWrapper		rootFilter	= null;
	private FilterLoader		filterLoader	= null;
	private DynamicClassLoader	classLoader	= null;

	public FilterService(ServerContext context) {

		this.classLoader = new DynamicClassLoader();

		this.context = context;

	}

	public void accept(Request request, Response response) throws IOException {

		try {
			accept(rootFilter, request, (InnerResponse) response);
		} catch (FlushedException e) {
			logger.error(e.getMessage(), e);
		} catch (NIOException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			this.acceptException(e, request, response);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			this.acceptException(e, request, response);
		}
	}

	private void acceptException(Exception exception, Request request, Response response) throws IOException {

		ErrorServlet servlet = new ErrorServlet(exception);

		try {

			servlet.accept(request, response);

		} catch (IOException e) {

			throw e;

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

		}
	}

	private boolean accept(NIOFilterWrapper filter, Request request, InnerResponse response) throws Exception {

		for (; filter != null;) {

			filter.accept(request, response);

			if (response.schduled()) {

				return true;
			}

			filter = filter.nextFilter();
		}
		return true;
	}

	protected void doStart() throws Exception {

		this.classLoader.scan(context.getAppLocalAddress());

		this.filterLoader = new NormalFilterLoader(context, classLoader);

		this.filterLoader.start();

		this.rootFilter = filterLoader.getRootFilter();

	}

	protected void doStop() throws Exception {
		LifeCycleUtil.stop(filterLoader);
	}

	public boolean redeploy() {

		logger.info(" [NIOServer] ======================================= 开始服务升级 =======================================");

		DynamicClassLoader classLoader = new DynamicClassLoader();

		FilterLoader filterLoader = new NormalFilterLoader(context, classLoader);

		try {

			logger.info(" [NIOServer] 加载项目组件包");

			classLoader.scan(context.getAppLocalAddress());

		} catch (IOException e) {

			logger.error(e.getMessage(), e);

			logger.info("      [NIOServer] ======================================= 服务升级失败 =======================================");

			return false;
		}

		try {

			filterLoader.prepare(context, null);

		} catch (Throwable e) {

			logger.error(e.getMessage(), e);

			logger.info("      [NIOServer] ======================================= 服务升级失败 =======================================");

			return false;
		}

		this.rootFilter = filterLoader.getRootFilter();

		this.subdeploy(this.filterLoader);

		this.filterLoader = filterLoader;

		this.classLoader.unload();

		this.classLoader = classLoader;

		logger.info("      [NIOServer] ======================================= 服务升级完成 =======================================");

		return true;

	}

	private void subdeploy(FilterLoader filterLoader) {

		try {

			filterLoader.unload(context, null);

		} catch (Throwable e) {

			logger.error(e.getMessage(), e);

		}
	}

}
