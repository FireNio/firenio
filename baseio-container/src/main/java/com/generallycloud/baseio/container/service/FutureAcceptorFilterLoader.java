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

import com.generallycloud.baseio.common.LoggerUtil;
import com.generallycloud.baseio.component.DynamicClassLoader;
import com.generallycloud.baseio.container.AbstractInitializeable;
import com.generallycloud.baseio.container.ApplicationContext;
import com.generallycloud.baseio.container.InitializeUtil;
import com.generallycloud.baseio.container.configuration.Configuration;
import com.generallycloud.baseio.container.configuration.FiltersConfiguration;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public class FutureAcceptorFilterLoader extends AbstractInitializeable {

    private Logger                      logger = LoggerFactory.getLogger(getClass());
    private FutureAcceptorFilterWrapper rootFilter;
    private ApplicationContext          context;
    private FiltersConfiguration        configuration;
    private FutureAcceptorServiceFilter serviceFilter;
    private List<FutureAcceptorFilter>  acceptorFilters = new ArrayList<>();

    public FutureAcceptorFilterLoader(FutureAcceptorServiceFilter serviceFilter) {
        this.serviceFilter = serviceFilter;
    }

    public FutureAcceptorServiceLoader getFutureAcceptorServiceLoader() {
        return serviceFilter.getFutureAcceptorServiceLoader();
    }

    private FutureAcceptorFilterWrapper loadFilters(ApplicationContext context,
            DynamicClassLoader classLoader) throws IOException, InstantiationException,
            IllegalAccessException, ClassNotFoundException {

        List<Configuration> filterConfigurations = configuration.getFilters();
        List<FutureAcceptorFilter> acceptorFilters = this.acceptorFilters;
        acceptorFilters.add(serviceFilter);

        if (filterConfigurations == null || filterConfigurations.isEmpty()) {
            LoggerUtil.prettyLog(logger, "did not found filter config");
            filterConfigurations = new ArrayList<>();
        }

        FutureAcceptorFilterWrapper rootFilter = null;
        FutureAcceptorFilterWrapper last = null;

        for (int i = 0; i < filterConfigurations.size(); i++) {
            Configuration filterConfig = filterConfigurations.get(i);
            String clazzName = filterConfig.getParameter("class", "empty");
            FutureAcceptorFilter filter = (FutureAcceptorFilter) classLoader.loadClass(clazzName)
                    .newInstance();
            filter.setConfig(filterConfig);
            int sortIndex = filterConfig.getIntegerParameter("sortIndex", 999);
            filter.setSortIndex(sortIndex);
            acceptorFilters.add(filter);
        }

        Collections.sort(acceptorFilters, new Comparator<FutureAcceptorFilter>() {
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

        for (int i = 0; i < acceptorFilters.size(); i++) {
            FutureAcceptorFilter filter = acceptorFilters.get(i);
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

    public List<FutureAcceptorFilter> getAcceptorFilters() {
        return acceptorFilters;
    }

    @Override
    public void destroy(ApplicationContext context, Configuration config) throws Exception {
        this.destroyFilters(rootFilter);
        this.acceptorFilters.clear();
        super.destroy(context, config);
    }

    @Override
    public void initialize(ApplicationContext context, Configuration config) throws Exception {
        this.context = context;
        this.configuration = context.getConfiguration().getFiltersConfiguration();
        this.rootFilter = loadFilters(context, context.getClassLoader());
        this.initializeFilters(rootFilter);
        super.initialize(context, config);
    }

    private void initializeFilters(FutureAcceptorFilterWrapper filter) throws Exception {
        for (; filter != null;) {
            filter.initialize(context, filter.getConfig());
            LoggerUtil.prettyLog(logger, "loaded [ {} ] ", filter);
            filter = filter.getNext();
        }
    }

    private void destroyFilters(FutureAcceptorFilterWrapper filter) {
        for (; filter != null;) {
            InitializeUtil.destroy(filter, context);
            LoggerUtil.prettyLog(logger, "unloaded [ {} ] ", filter);
            filter = filter.getNext();
        }
    }

}
