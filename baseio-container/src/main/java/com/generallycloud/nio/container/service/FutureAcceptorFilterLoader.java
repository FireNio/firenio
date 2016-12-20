/*
 * Copyright 2015 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package com.generallycloud.nio.container.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.generallycloud.nio.AbstractLifeCycle;
import com.generallycloud.nio.LifeCycle;
import com.generallycloud.nio.Linkable;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.LoggerUtil;
import com.generallycloud.nio.container.ApplicationContext;
import com.generallycloud.nio.container.DynamicClassLoader;
import com.generallycloud.nio.container.HotDeploy;
import com.generallycloud.nio.container.configuration.Configuration;
import com.generallycloud.nio.container.configuration.FiltersConfiguration;

public class FutureAcceptorFilterLoader extends AbstractLifeCycle implements HotDeploy, LifeCycle {

	private Logger						logger		= LoggerFactory.getLogger(FutureAcceptorFilterLoader.class);
	private Linkable<FutureAcceptorFilter>	rootFilter	;
	private ApplicationContext			context		;
	private DynamicClassLoader			classLoader	;
	private FiltersConfiguration			configuration	;
	private FutureAcceptorServiceFilter	serviceFilter	;

	public FutureAcceptorFilterLoader(ApplicationContext context, DynamicClassLoader classLoader,FutureAcceptorServiceFilter	serviceFilter) {
		this.configuration = context.getConfiguration().getFiltersConfiguration();
		this.context = context;
		this.classLoader = classLoader;
		this.serviceFilter = serviceFilter;
	}

	public FutureAcceptorServiceLoader getFutureAcceptorServiceLoader() {
		return serviceFilter.getFutureAcceptorServiceLoader();
	}

	private Linkable<FutureAcceptorFilter> loadFilters(ApplicationContext context, DynamicClassLoader classLoader)
			throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {

		List<Configuration> filterConfigurations = configuration.getFilters();

		List<FutureAcceptorFilter> filters = new ArrayList<FutureAcceptorFilter>();
		
		filters.add(serviceFilter);
		
		if (filterConfigurations == null || filterConfigurations.isEmpty()) {
			LoggerUtil.prettyNIOServerLog(logger, "没有配置Filter");
			filterConfigurations = new ArrayList<Configuration>();
		}

		Linkable<FutureAcceptorFilter> rootFilter = null;

		Linkable<FutureAcceptorFilter> last = null;

		for (int i = 0; i < filterConfigurations.size(); i++) {

			Configuration filterConfig = filterConfigurations.get(i);

			String clazzName = filterConfig.getParameter("class", "empty");

			FutureAcceptorFilter filter = (FutureAcceptorFilter) classLoader.forName(clazzName).newInstance();

			filter.setConfig(filterConfig);
			
			int sortIndex = filterConfig.getIntegerParameter("sortIndex",999);
			
			filter.setSortIndex(sortIndex);

			filters.add(filter);
		}

		filters.addAll(context.getPluginFilters());
		
		Collections.sort(filters,new Comparator<FutureAcceptorFilter>() {

			@Override
			public int compare(FutureAcceptorFilter o1, FutureAcceptorFilter o2) {
				
				int i1 = o1.getSortIndex();
				int i2 = o2.getSortIndex();
				
				if (i1 == i2) {
					return 0;
				}
				
				return i1 > i2 ? 1 : -1;
			}
		});

		for (int i = 0; i < filters.size(); i++) {

			FutureAcceptorFilter filter = filters.get(i);

			Linkable<FutureAcceptorFilter> _filter = new FutureAcceptorFilterWrapper(context, filter,
					filter.getConfig());

			if (last == null) {

				last = _filter;

				rootFilter = _filter;
			} else {

				last.setNext(_filter);

				last = _filter;
			}
		}

		return rootFilter;
	}

	public Linkable<FutureAcceptorFilter> getRootFilter() {
		return rootFilter;
	}

	@Override
	protected void doStart() throws Exception {
		this.rootFilter = this.loadFilters(context, classLoader);

		// start all filter
		this.initializeFilters(rootFilter);
	}

	private void initializeFilters(Linkable<FutureAcceptorFilter> filter) throws Exception {

		for (; filter != null;) {

			FutureAcceptorFilter acceptorFilter = filter.getValue();
			
			acceptorFilter.initialize(context, acceptorFilter.getConfig());

			LoggerUtil.prettyNIOServerLog(logger, "加载完成 [ {} ] ", filter);

			filter = filter.getNext();
		}
		
	}

	private void destroyFilters(Linkable<FutureAcceptorFilter> filter) {

		for (; filter != null;) {

			try {
				
				FutureAcceptorFilter acceptorFilter = filter.getValue();
				
				acceptorFilter.destroy(context, acceptorFilter.getConfig());
				
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}

			LoggerUtil.prettyNIOServerLog(logger, "卸载完成 [ {} ] ", filter);

			filter = filter.getNext();

		}
	}

	@Override
	protected void doStop() throws Exception {
		this.destroyFilters(rootFilter);
	}

	private void prepare(Linkable<FutureAcceptorFilter> filter) throws Exception {

		for (; filter != null;) {

			FutureAcceptorFilter acceptorFilter = filter.getValue();
			
			acceptorFilter.prepare(context, acceptorFilter.getConfig());

			LoggerUtil.prettyNIOServerLog(logger, "新的Filter  [ {} ] Prepare完成", filter);

			filter = filter.getNext();
		}
	}

	@Override
	public void prepare(ApplicationContext context, Configuration config) throws Exception {

		LoggerUtil.prettyNIOServerLog(logger, "尝试加载新的Filter配置......");

		this.rootFilter = loadFilters(context, classLoader);

		LoggerUtil.prettyNIOServerLog(logger, "尝试启动新的Filter配置......");

		this.prepare(rootFilter);

		this.softStart();
	}

	@Override
	public void unload(ApplicationContext context, Configuration config) throws Exception {

		Linkable<FutureAcceptorFilter> filter = rootFilter;

		for (; filter != null;) {

			try {
				
				FutureAcceptorFilter acceptorFilter = filter.getValue();
				
				acceptorFilter.unload(context, acceptorFilter.getConfig());

				LoggerUtil.prettyNIOServerLog(logger, "旧的Filter  [ {} ] Unload完成", filter);

			} catch (Throwable e) {
				// ignore
				LoggerUtil.prettyNIOServerLog(logger, "旧的Filter  [ {} ] Unload失败", filter);
				logger.error(e.getMessage(), e);
			}

			filter = filter.getNext();

		}
	}

}
