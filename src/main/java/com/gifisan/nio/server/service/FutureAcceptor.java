package com.gifisan.nio.server.service;

import java.io.IOException;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.LifeCycle;
import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.ApplicationContext;
import com.gifisan.nio.component.DynamicClassLoader;
import com.gifisan.nio.component.IOEventHandle;
import com.gifisan.nio.component.PluginContext;
import com.gifisan.nio.component.ReadFutureAcceptor;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.server.service.impl.ErrorServlet;

//FIXME exception
public final class FutureAcceptor extends AbstractLifeCycle implements LifeCycle, ReadFutureAcceptor {

	private Logger						logger		= LoggerFactory.getLogger(FutureAcceptor.class);
	private ApplicationContext			context		= null;
	private FutureAcceptorFilterWrapper	rootFilter	= null;
	private FutureAcceptorFilterLoader		filterLoader	= null;
	private DynamicClassLoader			classLoader	= null;
	private PluginLoader				pluginLoader	= null;

	public FutureAcceptor(ApplicationContext context, DynamicClassLoader classLoader) {

		this.classLoader = classLoader;

		this.context = context;
	}

	public void accept(Session session, ReadFuture future) throws IOException {

		try {

			accept(rootFilter, session, future);

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

		} catch (Throwable e) {

			logger.error(e.getMessage(), e);
		}
	}

	private boolean accept(FutureAcceptorFilterWrapper filter, Session session, ReadFuture future) {

		for (; filter != null;) {

			try {
				
				future.setIOEventHandle(filter);
				
				filter.accept(session, future);
				
			} catch (Exception e) {
				
				IOEventHandle eventHandle = future.getIOEventHandle();
				
				eventHandle.exceptionCaughtOnRead(session, future, e);
				
				return true;
			}

			if (future.flushed()) {

				return true;
			}

			filter = filter.nextFilter();
		}
		return true;
	}

	protected void doStart() throws Exception {

		this.classLoader.scan(context.getAppLocalAddress());

		this.pluginLoader = new PluginLoader(context, classLoader);

		this.filterLoader = new FutureAcceptorFilterLoader(context, classLoader);

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

		FutureAcceptorFilterLoader filterLoader = new FutureAcceptorFilterLoader(context, classLoader);

		PluginLoader pluginLoader = new PluginLoader(context, classLoader);

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

	private void unloadFilterLoader(FutureAcceptorFilterLoader filterLoader) {

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
