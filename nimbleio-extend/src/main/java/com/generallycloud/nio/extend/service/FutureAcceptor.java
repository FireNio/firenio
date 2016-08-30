package com.generallycloud.nio.extend.service;

import java.io.IOException;

import com.generallycloud.nio.AbstractLifeCycle;
import com.generallycloud.nio.LifeCycle;
import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.IOEventHandle;
import com.generallycloud.nio.component.ReadFutureAcceptor;
import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.component.IOEventHandle.IOEventState;
import com.generallycloud.nio.component.protocol.ReadFuture;
import com.generallycloud.nio.extend.ApplicationContext;
import com.generallycloud.nio.extend.DynamicClassLoader;
import com.generallycloud.nio.extend.PluginContext;
import com.generallycloud.nio.extend.implementation.ErrorServlet;

//FIXME exception
public final class FutureAcceptor extends AbstractLifeCycle implements LifeCycle, ReadFutureAcceptor {

	private DynamicClassLoader			classLoader	;
	private ApplicationContext			context		;
	private FutureAcceptorFilterLoader		filterLoader	;
	private Logger						logger		= LoggerFactory.getLogger(FutureAcceptor.class);
	private PluginLoader				pluginLoader	;
	private FutureAcceptorFilterWrapper	rootFilter	;
	private FutureAcceptorServiceFilter	serviceFilter	;
	
	public FutureAcceptor(ApplicationContext context, DynamicClassLoader classLoader,FutureAcceptorServiceFilter	serviceFilter) {

		this.classLoader = classLoader;

		this.context = context;
		
		this.serviceFilter = serviceFilter;
	}

	private boolean accept(FutureAcceptorFilterWrapper filter, Session session, ReadFuture future) {

		for (; filter != null;) {

			try {
				
				future.setIOEventHandle(filter);
				
				filter.accept(session, future);
				
			} catch (Exception e) {
				
				logger.error(e.getMessage(),e);
				
				IOEventHandle eventHandle = future.getIOEventHandle();
				
				eventHandle.exceptionCaught(session, future, e, IOEventState.HANDLE);
				
				return true;
			}

			if (future.flushed()) {

				return true;
			}

			filter = filter.nextFilter();
		}
		return true;
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

	protected void doStart() throws Exception {

		this.classLoader.scan(context.getAppLocalAddress());

		this.pluginLoader = new PluginLoader(context, classLoader);

		this.filterLoader = new FutureAcceptorFilterLoader(context, classLoader,serviceFilter);

		LifeCycleUtil.start(pluginLoader);
		
		LifeCycleUtil.start(filterLoader);

		this.rootFilter = filterLoader.getRootFilter();

	}

	protected void doStop() throws Exception {
		LifeCycleUtil.stop(filterLoader);
		LifeCycleUtil.stop(pluginLoader);
	}

	public FutureAcceptorServiceLoader getFutureAcceptorServiceLoader() {
		return filterLoader.getFutureAcceptorServiceLoader();
	}

	public PluginContext[] getPluginContexts() {
		return pluginLoader.getPluginContexts();
	}

	public boolean redeploy(DynamicClassLoader classLoader) {

		logger.info("       [NIOServer] ======================================= 开始服务升级 =======================================");

		FutureAcceptorFilterLoader filterLoader = new FutureAcceptorFilterLoader(context, classLoader,serviceFilter);

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

}
