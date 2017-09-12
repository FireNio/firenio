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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.generallycloud.baseio.common.LoggerUtil;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.DynamicClassLoader;
import com.generallycloud.baseio.container.AbstractInitializeable;
import com.generallycloud.baseio.container.ApplicationContext;
import com.generallycloud.baseio.container.InitializeUtil;
import com.generallycloud.baseio.container.configuration.Configuration;
import com.generallycloud.baseio.container.configuration.ServicesConfiguration;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public class FutureAcceptorServiceLoader extends AbstractInitializeable {

    private ApplicationContext                 context;
    private DynamicClassLoader                 classLoader;
    private ServicesConfiguration              configuration;
    private Logger                             logger   = LoggerFactory.getLogger(getClass());
    private Map<String, FutureAcceptorService> services = new HashMap<>();

    @Override
    public void destroy(ApplicationContext context, Configuration config) throws Exception {
        for (FutureAcceptorService entry : services.values()) {
            InitializeUtil.destroy(entry, context);
            LoggerUtil.prettyLog(logger, "unloaded [ {} ]", entry);
        }
        services.clear();
        super.destroy(context, config);
    }

    @Override
    public void initialize(ApplicationContext context, Configuration config) throws Exception {
        this.context = context;
        this.classLoader = context.getClassLoader();
        this.configuration = context.getConfiguration().getServletsConfiguration();
        this.loadServices(configuration, classLoader);
        this.initializeServices(services);
        super.initialize(context, config);
    }

    public FutureAcceptorService getFutureAcceptor(String serviceName) {
        return services.get(serviceName);
    }

    private void initializeServices(Map<String, FutureAcceptorService> services) throws Exception {
        Collection<FutureAcceptorService> es = services.values();
        for (FutureAcceptorService e : es) {
            e.initialize(context, e.getConfig());
            LoggerUtil.prettyLog(logger, "loaded [ {} ]", e);
        }
    }

    private void loadServices(ServicesConfiguration configuration,
            DynamicClassLoader classLoader) throws Exception {
        if (configuration.getServices().size() == 0) {
            logger.info("no services configed");
        }
        for (Configuration c : configuration.getServices()) {
            try {
                loadService(c);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
    
    public Map<String, FutureAcceptorService> getServices() {
        return services;
    }

    private void loadService(Configuration config)
            throws Exception {
        Map<String, FutureAcceptorService> services = this.services;
        String className = config.getParameter("class", "empty");
        Class<?> clazz = classLoader.loadClass(className);
        String serviceName = config.getParameter("service-name");
        if (StringUtil.isNullOrBlank(serviceName)) {
            throw new IllegalArgumentException("null service name,class :" + className);
        }
        if (services.containsKey(serviceName)) {
            throw new IllegalArgumentException(
                    "repeat servlet[ " + serviceName + "@" + className + " ]");
        }
        FutureAcceptorService service = (FutureAcceptorService) clazz.newInstance();
        service.setServiceName(serviceName);
        services.put(serviceName, service);
        service.setConfig(config);
    }

}
