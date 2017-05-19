/*
 * Copyright 2015-2017 GenerallyCloud.com
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
package com.generallycloud.baseio.container.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.generallycloud.baseio.AbstractLifeCycle;
import com.generallycloud.baseio.LifeCycle;
import com.generallycloud.baseio.common.Logger;
import com.generallycloud.baseio.common.LoggerFactory;
import com.generallycloud.baseio.common.LoggerUtil;
import com.generallycloud.baseio.component.Linkable;
import com.generallycloud.baseio.container.ApplicationContext;
import com.generallycloud.baseio.container.DynamicClassLoader;
import com.generallycloud.baseio.container.configuration.Configuration;
import com.generallycloud.baseio.container.configuration.FiltersConfiguration;

public class FutureAcceptorFilterLoader extends AbstractLifeCycle implements LifeCycle {

	private Logger						logger		= LoggerFactory.getLogger(FutureAcceptorFilterLoader.class);
	private FutureAcceptorFilterWrapper	rootFilter	;
	private ApplicationContext			context		;
	private FiltersConfiguration			configuration	;
	private FutureAcceptorServiceFilter	serviceFilter	;

	public FutureAcceptorFilterLoader(ApplicationContext context, FutureAcceptorServiceFilter	serviceFilter) {
		this.configuration = context.getConfiguration().getFiltersConfiguration();
		this.context = context;
		this.serviceFilter = serviceFilter;
	}

	public FutureAcceptorServiceLoader getFutureAcceptorServiceLoader() {
		return serviceFilter.getFutureAcceptorServiceLoader();
	}

	private FutureAcceptorFilterWrapper loadFilters(ApplicationContext context, DynamicClassLoader classLoader)
			throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {

		List<Configuration> filterConfigurations = configuration.getFilters();

		List<FutureAcceptorFilter> filters = new ArrayList<FutureAcceptorFilter>();
		
		filters.add(serviceFilter);
		
		if (filterConfigurations == null || filterConfigurations.isEmpty()) {
			LoggerUtil.prettyNIOServerLog(logger, "did not found filter config");
			filterConfigurations = new ArrayList<Configuration>();
		}

		FutureAcceptorFilterWrapper rootFilter = null;

		FutureAcceptorFilterWrapper last = null;

		for (int i = 0; i < filterConfigurations.size(); i++) {

			Configuration filterConfig = filterConfigurations.get(i);

			String clazzName = filterConfig.getParameter("class", "empty");

			FutureAcceptorFilter filter = (FutureAcceptorFilter) classLoader.loadClass(clazzName).newInstance();

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

			FutureAcceptorFilterWrapper _filter = new FutureAcceptorFilterWrapper(context, filter,
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

	public FutureAcceptorFilterWrapper getRootFilter() {
		return rootFilter;
	}

	@Override
	protected void doStart() throws Exception {
		this.rootFilter = this.loadFilters(context,context.getClassLoader());

		// start all filter
		this.initializeFilters(rootFilter);
	}

	private void initializeFilters(Linkable<FutureAcceptorFilter> filter) throws Exception {

		for (; filter != null;) {

			FutureAcceptorFilter acceptorFilter = filter.getValue();
			
			acceptorFilter.initialize(context, acceptorFilter.getConfig());

			LoggerUtil.prettyNIOServerLog(logger, "loaded [ {} ] ", filter);

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

			LoggerUtil.prettyNIOServerLog(logger, "unloaded [ {} ] ", filter);

			filter = filter.getNext();

		}
	}

	@Override
	protected void doStop() throws Exception {
		this.destroyFilters(rootFilter);
	}

}
