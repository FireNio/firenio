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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.generallycloud.nio.AbstractLifeCycle;
import com.generallycloud.nio.LifeCycle;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.LoggerUtil;
import com.generallycloud.nio.container.ApplicationContext;
import com.generallycloud.nio.container.DynamicClassLoader;
import com.generallycloud.nio.container.configuration.Configuration;
import com.generallycloud.nio.container.configuration.ServicesConfiguration;

public class FutureAcceptorServiceLoader extends AbstractLifeCycle implements LifeCycle {

	private ApplicationContext				context;
	private DynamicClassLoader				classLoader;
	private ServicesConfiguration				configuration;
	private Logger							logger	= LoggerFactory.getLogger(getClass());
	private Map<String, FutureAcceptorService>	services	= new LinkedHashMap<String, FutureAcceptorService>();

	public FutureAcceptorServiceLoader(ApplicationContext context, DynamicClassLoader classLoader) {
		this.configuration = context.getConfiguration().getServletsConfiguration();
		this.context = context;
		this.classLoader = classLoader;
	}

	@Override
	protected void doStart() throws Exception {

		Map<String, FutureAcceptorService> servlets = loadServlets(configuration, classLoader);

		this.initializeServlets(servlets);

		this.services = servlets;

	}

	@Override
	protected void doStop() throws Exception {

		Set<Entry<String, FutureAcceptorService>> entries = services.entrySet();

		for (Entry<String, FutureAcceptorService> entry : entries) {

			FutureAcceptorService servlet = entry.getValue();

			try {

				servlet.destroy(context, servlet.getConfig());

				LoggerUtil.prettyNIOServerLog(logger, "卸载完成 [ {} ]", servlet);

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

	private void initializeServlets(Map<String, FutureAcceptorService> servlets) throws Exception {

		Set<Entry<String, FutureAcceptorService>> entries = servlets.entrySet();

		for (Entry<String, FutureAcceptorService> entry : entries) {

			FutureAcceptorService servlet = entry.getValue();

			servlet.initialize(context, servlet.getConfig());

			LoggerUtil.prettyNIOServerLog(logger, "加载完成 [ {} ]", servlet);

		}
	}

	private Map<String, FutureAcceptorService> loadServlets(ServicesConfiguration configuration,
			DynamicClassLoader classLoader) throws Exception {

		List<Configuration> servletConfigurations = configuration.getServlets();

		Map<String, FutureAcceptorService> pluginServlets = context.getPluginServlets();

		Map<String, FutureAcceptorService> servlets = new LinkedHashMap<String, FutureAcceptorService>();

		servlets.putAll(pluginServlets);

		if (servletConfigurations.size() == 0) {

			if (servlets.size() == 0) {

				throw new Error("empty servlet config");
			}
			return servlets;
		}

		for (int i = 0; i < servletConfigurations.size(); i++) {

			Configuration config = servletConfigurations.get(i);

			String className = config.getParameter("class", "empty");

			Class<?> clazz = classLoader.forName(className);

			String serviceName = config.getParameter("service-name", clazz.getSimpleName());

			if (servlets.containsKey(serviceName)) {
				throw new IllegalArgumentException("repeat servlet[ " + serviceName + "@" + className + " ]");
			}

			FutureAcceptorService servlet = (FutureAcceptorService) clazz.newInstance();

			servlets.put(serviceName, servlet);

			servlet.setConfig(config);

		}
		return servlets;
	}

}
