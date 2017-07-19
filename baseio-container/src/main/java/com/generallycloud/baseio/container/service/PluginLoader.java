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

import java.util.List;

import com.generallycloud.baseio.AbstractLifeCycle;
import com.generallycloud.baseio.LifeCycle;
import com.generallycloud.baseio.common.LoggerUtil;
import com.generallycloud.baseio.container.ApplicationContext;
import com.generallycloud.baseio.container.DynamicClassLoader;
import com.generallycloud.baseio.container.PluginContext;
import com.generallycloud.baseio.container.configuration.Configuration;
import com.generallycloud.baseio.container.configuration.PluginsConfiguration;
import com.generallycloud.baseio.log.Logger;
import com.generallycloud.baseio.log.LoggerFactory;

public class PluginLoader extends AbstractLifeCycle implements LifeCycle {

	private ApplicationContext	context;
	private Logger				logger	= LoggerFactory.getLogger(PluginLoader.class);
	private PluginContext[]		pluginContexts;
	private PluginsConfiguration	configuration;

	public PluginLoader(ApplicationContext context) {
		this.configuration = context.getConfiguration().getPluginsConfiguration();
		this.context = context;
	}

	@Override
	protected void doStart() throws Exception {

		loadPlugins(context, context.getClassLoader(), this.configuration);

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
				LoggerUtil.prettyLog(logger, "unloaded [ {} ]", plugin);
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

			LoggerUtil.prettyLog(logger, "loaded [ {} ]", plugin);
		}
	}

	private void loadPlugins(ApplicationContext context, DynamicClassLoader classLoader,
			PluginsConfiguration configuration) throws Exception {

		List<Configuration> plugins = configuration.getPlugins();

		pluginContexts = new PluginContext[plugins.size()];

		for (int i = 0; i < plugins.size(); i++) {

			try {
				pluginContexts[i] = loadPlugin(plugins.get(i),classLoader);
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
		}
	}
	
	private PluginContext loadPlugin(Configuration config,DynamicClassLoader classLoader) throws Exception{
		
		String className = config.getParameter("class", "empty");

		Class<?> clazz = classLoader.loadClass(className);

		PluginContext plugin = (PluginContext) clazz.newInstance();

		plugin.setConfig(config);
		
		return plugin;
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
