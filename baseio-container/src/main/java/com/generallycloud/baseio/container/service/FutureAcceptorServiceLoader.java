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
import java.util.List;
import java.util.Map;

import com.generallycloud.baseio.AbstractLifeCycle;
import com.generallycloud.baseio.LifeCycle;
import com.generallycloud.baseio.common.LoggerUtil;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.container.ApplicationContext;
import com.generallycloud.baseio.container.DynamicClassLoader;
import com.generallycloud.baseio.container.configuration.Configuration;
import com.generallycloud.baseio.container.configuration.ServicesConfiguration;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public class FutureAcceptorServiceLoader extends AbstractLifeCycle implements LifeCycle {

	private ApplicationContext				context;
	private DynamicClassLoader				classLoader;
	private ServicesConfiguration				configuration;
	private Logger							logger	= LoggerFactory.getLogger(getClass());
	private Map<String, FutureAcceptorService>	services	= new HashMap<String, FutureAcceptorService>();

	public FutureAcceptorServiceLoader(ApplicationContext context) {
		this.context = context;
		this.classLoader = context.getClassLoader();
		this.configuration = context.getConfiguration().getServletsConfiguration();
	}

	@Override
	protected void doStart() throws Exception {

		this.services = loadServices(configuration, classLoader);

		this.initializeServices(services);
	}

	@Override
	protected void doStop() throws Exception {

		Collection<FutureAcceptorService> entries = services.values();

		for (FutureAcceptorService entry : entries) {

			try {

				entry.destroy(context, entry.getConfig());

				LoggerUtil.prettyLog(logger, "unloaded [ {} ]", entry);

			} catch (Throwable e) {

				logger.error(e.getMessage(), e);
			}
		}
	}

	public FutureAcceptorService getFutureAcceptor(String serviceName) {
		return services.get(serviceName);
	}

	public void listen(String serviceName, FutureAcceptorService service) {
		this.services.put(serviceName, service);
	}

	public void listen(Map<String, FutureAcceptorService> services) {
		this.services.putAll(services);
	}

	private void initializeServices(Map<String, FutureAcceptorService> services) throws Exception {

		Collection<FutureAcceptorService> es = services.values();

		for (FutureAcceptorService e : es) {

			e.initialize(context, e.getConfig());

			LoggerUtil.prettyLog(logger, "loaded [ {} ]", e);
		}
	}
	
	private Map<String, FutureAcceptorService> loadServices(ServicesConfiguration configuration,
			DynamicClassLoader classLoader) throws Exception {

		List<Configuration> servletConfigurations = configuration.getServlets();
		
		if (servletConfigurations.size() == 0) {
			logger.info("no servlet configed");
		}
		
		Map<String, FutureAcceptorService> servlets = new HashMap<>();
		
		for (int i = 0; i < servletConfigurations.size(); i++) {
			try {
				loadService(servlets,servletConfigurations.get(i));
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
		}
		
		servlets.putAll(context.getPluginServlets());
		
		return servlets;
	}
	
	private void loadService(Map<String, FutureAcceptorService> servlets,Configuration config) throws Exception{
		
		String className = config.getParameter("class", "empty");
		
		Class<?> clazz = classLoader.loadClass(className);

		String serviceName = config.getParameter("service-name");
		
		if (StringUtil.isNullOrBlank(serviceName)) {
			throw new IllegalArgumentException("null service name,class :"+className);
		}

		if (servlets.containsKey(serviceName)) {
			throw new IllegalArgumentException("repeat servlet[ " + serviceName + "@" + className + " ]");
		}

		FutureAcceptorService servlet = (FutureAcceptorService) clazz.newInstance();
		
		servlet.setServiceName(serviceName);

		servlets.put(serviceName, servlet);

		servlet.setConfig(config);
	}

}
