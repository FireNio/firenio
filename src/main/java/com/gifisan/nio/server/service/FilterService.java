package com.gifisan.nio.server.service;

import java.io.IOException;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.FlushedException;
import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.ApplicationContext;
import com.gifisan.nio.component.DynamicClassLoader;
import com.gifisan.nio.component.PluginContext;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.server.FilterAcceptor;
import com.gifisan.nio.server.service.impl.ErrorServlet;

public final class FilterService extends AbstractLifeCycle implements LifeCycle,FilterAcceptor {

	private Logger				logger		= LoggerFactory.getLogger(FilterService.class);
	private ApplicationContext	context		= null;
	private NIOFilterWrapper		rootFilter	= null;
	private FilterLoader		filterLoader	= null;
	private DynamicClassLoader	classLoader	= null;
	private PluginLoader		pluginLoader	= null;

	public FilterService(ApplicationContext context, DynamicClassLoader classLoader) {

		this.classLoader = classLoader;

		this.context = context;
	}

	public void accept(Session session, ReadFuture future) throws IOException {

		try {

			accept(rootFilter, session, future);

		} catch (FlushedException e) {

			logger.error(e.getMessage(), e);

		} catch (Throwable e) {

			logger.error(e.getMessage(), e);

			this.acceptException(session, future, e);
		}
	}

	private void acceptException(Session session, ReadFuture future, Throwable exception) throws IOException {

		ErrorServlet servlet = new ErrorServlet(exception);

		try {

			servlet.accept(session, future);

		} catch (IOException e) {

			throw e;

		} catch (Exception e) {

			logger.error(e.getMessage(), e);
		}
	}

	private boolean accept(NIOFilterWrapper filter, Session session, ReadFuture future) throws Exception {

		for (; filter != null;) {

			filter.accept(session, future);

			if (future.flushed()) {

				return true;
			}

			filter = filter.nextFilter();
		}
		return true;
	}

	protected void doStart() throws Exception {

		this.classLoader.scan(context.getAppLocalAddress());

		this.pluginLoader = new NormalPluginLoader(context, classLoader);

		this.filterLoader = new NormalFilterLoader(context, classLoader);

		this.pluginLoader.start();

		this.filterLoader.start();

		this.rootFilter = filterLoader.getRootFilter();

	}

	protected void doStop() throws Exception {
		LifeCycleUtil.stop(filterLoader);
		LifeCycleUtil.stop(pluginLoader);
	}

	public boolean redeploy(DynamicClassLoader classLoader) {

		logger.info("       [NIOServer] ======================================= 开始服务升级 =======================================");

		FilterLoader filterLoader = new NormalFilterLoader(context, classLoader);

		PluginLoader pluginLoader = new NormalPluginLoader(context, classLoader);

		try {

			logger.info("       [NIOServer] 加载项目组件包");

			classLoader.scan(context.getAppLocalAddress());

		} catch (IOException e) {

			logger.error(e.getMessage(), e);

			logger.info("       [NIOServer] ======================================= 服务升级失败 =======================================");

			return false;
		}

		try {

			pluginLoader.prepare(context, null);

		} catch (Throwable e) {

			logger.error(e.getMessage(), e);

			logger.info("       [NIOServer] ======================================= 服务升级失败 =======================================");

			return false;
		}

		try {

			filterLoader.prepare(context, null);

		} catch (Throwable e) {

			logger.error(e.getMessage(), e);

			logger.info("       [NIOServer] ======================================= 服务升级失败 =======================================");

			return false;
		}

		this.rootFilter = filterLoader.getRootFilter();

		this.unloadFilterLoader(this.filterLoader);

		this.unloadPluginLoader(this.pluginLoader);

		this.filterLoader = filterLoader;

		this.pluginLoader = pluginLoader;

		this.classLoader.unload();

		this.classLoader = classLoader;

		logger.info("       [NIOServer] ======================================= 服务升级完成 =======================================");

		return true;

	}

	private void unloadFilterLoader(FilterLoader filterLoader) {

		try {

			filterLoader.unload(context, null);

		} catch (Throwable e) {

			logger.error(e.getMessage(), e);
		}
	}

	private void unloadPluginLoader(PluginLoader pluginLoader) {

		try {

			pluginLoader.unload(context, null);

		} catch (Throwable e) {

			logger.error(e.getMessage(), e);
		}
	}

	public PluginContext[] getPluginContexts() {
		return pluginLoader.getPluginContexts();
	}

}
