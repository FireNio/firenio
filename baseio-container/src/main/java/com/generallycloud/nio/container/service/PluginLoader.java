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

import java.util.List;

import com.generallycloud.nio.AbstractLifeCycle;
import com.generallycloud.nio.LifeCycle;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.LoggerUtil;
import com.generallycloud.nio.container.ApplicationContext;
import com.generallycloud.nio.container.DynamicClassLoader;
import com.generallycloud.nio.container.PluginContext;
import com.generallycloud.nio.container.configuration.Configuration;
import com.generallycloud.nio.container.configuration.PluginsConfiguration;

public class PluginLoader extends AbstractLifeCycle implements LifeCycle {

	private ApplicationContext	context;
	private DynamicClassLoader	classLoader;
	private Logger				logger	= LoggerFactory.getLogger(PluginLoader.class);
	private PluginContext[]		pluginContexts;
	private PluginsConfiguration	configuration;

	public PluginLoader(ApplicationContext context, DynamicClassLoader classLoader) {
		this.configuration = context.getConfiguration().getPluginsConfiguration();
		this.context = context;
		this.classLoader = classLoader;
	}

	@Override
	protected void doStart() throws Exception {

		loadPlugins(context, this.classLoader, this.configuration);

		this.initializePlugins(pluginContexts);

		this.configPluginFilterAndServlet(context);
	}

	@Override
	protected void doStop() throws Exception {

		for (PluginContext plugin : pluginContexts) {

			if (plugin == null) {
				continue;
			}

			try {

				plugin.destroy(context, plugin.getConfig());

				LoggerUtil.prettyNIOServerLog(logger, "卸载完成 [ {} ]", plugin);

			} catch (Throwable e) {

				logger.error(e.getMessage(), e);
			}
		}
	}

	public PluginContext[] getPluginContexts() {
		return pluginContexts;
	}

	private void initializePlugins(PluginContext[] plugins) throws Exception {

		for (PluginContext plugin : plugins) {

			if (plugin == null) {
				continue;
			}

			plugin.initialize(context, plugin.getConfig());

			LoggerUtil.prettyNIOServerLog(logger, "加载完成 [ {} ]", plugin);
		}
	}

	private void loadPlugins(ApplicationContext context, DynamicClassLoader classLoader,
			PluginsConfiguration configuration) throws Exception {

		List<Configuration> plugins = configuration.getPlugins();

		pluginContexts = new PluginContext[plugins.size()];

		for (int i = 0; i < plugins.size(); i++) {

			Configuration config = plugins.get(i);

			String className = config.getParameter("class", "empty");

			Class<?> clazz = classLoader.forName(className);

			PluginContext plugin;
			try {
				plugin = (PluginContext) clazz.newInstance();
			} catch (Exception e) {
				throw new RuntimeException("class instance failed :" + className, e);
			}

			pluginContexts[i] = plugin;

			plugin.setConfig(config);

		}
	}

	private void configPluginFilterAndServlet(ApplicationContext context) {

		for (PluginContext pluginContext : pluginContexts) {

			if (pluginContext == null) {
				continue;
			}

			pluginContext.configFutureAcceptorFilter(context.getPluginFilters());
			pluginContext.configFutureAcceptor(context.getPluginServlets());
		}
	}

}
