package com.gifisan.nio.server.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.component.ApplicationContext;
import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.component.DynamicClassLoader;
import com.gifisan.nio.server.configuration.FiltersConfiguration;
import com.gifisan.nio.server.service.impl.AuthorityFilter;

public class NormalFilterLoader extends AbstractLifeCycle implements FutureAcceptorFilterLoader {

	private Logger				logger		= LoggerFactory.getLogger(NormalFilterLoader.class);
	private ReadFutureAcceptorFilterWrapper		rootFilter	= null;
	private ApplicationContext	context		= null;
	private DynamicClassLoader	classLoader	= null;
	private FiltersConfiguration	configuration	= null;

	public NormalFilterLoader(ApplicationContext context, DynamicClassLoader classLoader) {
		this.configuration = context.getConfiguration().getFiltersConfiguration();
		this.context = context;
		this.classLoader = classLoader;
	}

	private ReadFutureAcceptorFilterWrapper loadFilters(ApplicationContext context, DynamicClassLoader classLoader)
			throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {

		List<Configuration> filterConfigurations = configuration.getFilters();

		List<ReadFutureAcceptorFilter> filters = new ArrayList<ReadFutureAcceptorFilter>();

		filters.add(new AuthorityFilter());

		if (filterConfigurations == null || filterConfigurations.isEmpty()) {
			logger.info(" [NIOServer] 没有配置Filter");
			filterConfigurations = new ArrayList<Configuration>();
		}

		ReadFutureAcceptorFilterWrapper rootFilter = null;

		ReadFutureAcceptorFilterWrapper last = null;

		for (int i = 0; i < filterConfigurations.size(); i++) {

			Configuration filterConfig = filterConfigurations.get(i);

			String clazzName = filterConfig.getParameter("class", "empty");

			ReadFutureAcceptorFilter filter = (ReadFutureAcceptorFilter) classLoader.forName(clazzName).newInstance();

			filter.setConfig(filterConfig);

			filters.add(filter);
		}

		filters.addAll(context.getPluginFilters());

		for (int i = 0; i < filters.size(); i++) {

			ReadFutureAcceptorFilter filter = filters.get(i);

			DefaultFutureAcceptorFilterWrapper _filter = new DefaultFutureAcceptorFilterWrapper(context, filter, filter.getConfig());

			if (last == null) {

				last = _filter;

				rootFilter = _filter;
			} else {

				last.setNextFilter(_filter);

				last = _filter;
			}
		}

		DefaultFutureAcceptorFilterWrapper filter = new DefaultFutureAcceptorFilterWrapper(context, new FutureAcceptorFilter(classLoader), null);

		if (last == null) {
			rootFilter = filter;
		} else {
			last.setNextFilter(filter);
		}

		return rootFilter;
	}

	public ReadFutureAcceptorFilterWrapper getRootFilter() {
		return rootFilter;
	}

	protected void doStart() throws Exception {
		this.rootFilter = this.loadFilters(context, classLoader);

		// start all filter
		this.initializeFilters(rootFilter);
	}

	private void initializeFilters(ReadFutureAcceptorFilterWrapper filter) throws Exception {

		for (; filter != null;) {

			filter.initialize(context, filter.getConfig());

			logger.info("  [NIOServer] 加载完成 [ {} ] ", filter);

			filter = filter.nextFilter();

		}
	}

	private void destroyFilters(ReadFutureAcceptorFilterWrapper filter) {

		for (; filter != null;) {

			try {
				filter.destroy(context, filter.getConfig());
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}

			logger.info("  [NIOServer] 卸载完成 [ {} ] ", filter);

			filter = filter.nextFilter();

		}
	}

	protected void doStop() throws Exception {
		this.destroyFilters(rootFilter);
	}

	private void prepare(ReadFutureAcceptorFilterWrapper filter) throws Exception {

		for (; filter != null;) {

			filter.prepare(context, filter.getConfig());

			logger.info("  [NIOServer] 新的Filter  [ {} ] Prepare完成", filter);

			filter = filter.nextFilter();
		}
	}

	public void prepare(ApplicationContext context, Configuration config) throws Exception {

		logger.info("  [NIOServer] 尝试加载新的Filter配置......");

		this.rootFilter = loadFilters(context, classLoader);

		logger.info("  [NIOServer] 尝试启动新的Filter配置......");

		this.prepare(rootFilter);

		this.softStart();
	}

	public void unload(ApplicationContext context, Configuration config) throws Exception {

		ReadFutureAcceptorFilterWrapper filter = rootFilter;

		for (; filter != null;) {

			try {
				filter.unload(context, filter.getConfig());

				logger.info("  [NIOServer] 旧的Filter  [ {} ] Unload完成", filter);

			} catch (Throwable e) {
				// ignore
				logger.info("  [NIOServer] 旧的Filter  [ {} ] Unload失败", filter);
				logger.error(e.getMessage(), e);
			}

			filter = filter.nextFilter();

		}
	}

}
