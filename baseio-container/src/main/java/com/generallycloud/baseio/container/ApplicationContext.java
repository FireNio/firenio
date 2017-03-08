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
package com.generallycloud.baseio.container;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.generallycloud.baseio.common.Logger;
import com.generallycloud.baseio.common.LoggerFactory;
import com.generallycloud.baseio.common.LoggerUtil;
import com.generallycloud.baseio.common.SharedBundle;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSessionEventListener;
import com.generallycloud.baseio.container.authority.AuthorityLoginCenter;
import com.generallycloud.baseio.container.authority.RoleManager;
import com.generallycloud.baseio.container.configuration.ApplicationConfiguration;
import com.generallycloud.baseio.container.implementation.SystemRedeployServlet;
import com.generallycloud.baseio.container.implementation.SystemStopServerServlet;
import com.generallycloud.baseio.container.service.FutureAcceptor;
import com.generallycloud.baseio.container.service.FutureAcceptorFilter;
import com.generallycloud.baseio.container.service.FutureAcceptorService;
import com.generallycloud.baseio.container.service.FutureAcceptorServiceFilter;
import com.generallycloud.baseio.container.service.FutureAcceptorServiceLoader;
import com.generallycloud.baseio.live.AbstractLifeCycle;
import com.generallycloud.baseio.live.LifeCycleUtil;

public class ApplicationContext extends AbstractLifeCycle {

	private static ApplicationContext instance;

	public static ApplicationContext getInstance() {
		return instance;
	}

	private String							appPath		= "app/";
	private String							appLocalAddres;
	private Sequence						sequence		= new Sequence();
	private DynamicClassLoader				classLoader	= new DynamicClassLoader();
	private ApplicationConfiguration			configuration;
	private SocketChannelContext				context;
	private Charset						encoding;
	private FutureAcceptorService				appRedeployService;
	private FutureAcceptor					filterService	;
	private Logger							logger		= LoggerFactory.getLogger(getClass());
	private LoginCenter						loginCenter	= new AuthorityLoginCenter();
	private List<FutureAcceptorFilter>			pluginFilters	= new ArrayList<FutureAcceptorFilter>();
	private Map<String, FutureAcceptorService>	pluginServlets	= new HashMap<String, FutureAcceptorService>();
	private RoleManager						roleManager	= new RoleManager();
	private FutureAcceptorServiceLoader		acceptorServiceLoader;
	private FutureAcceptorServiceFilter		futureAcceptorServiceFilter;
	private Map<String, FutureAcceptorService>	services		= new LinkedHashMap<String, FutureAcceptorService>();

	public ApplicationContext(ApplicationConfiguration configuration) {
		this.configuration = configuration;
	}

	@Override
	protected void doStart() throws Exception {

		if (context == null) {
			throw new IllegalArgumentException("null nio context");
		}
		
		if (futureAcceptorServiceFilter == null) {
			this.futureAcceptorServiceFilter = new FutureAcceptorServiceFilter();
		}

		if (appRedeployService == null) {
			appRedeployService = new SystemRedeployServlet();
		}
		
		instance = this;

		SharedBundle bundle = SharedBundle.instance();

		this.encoding = context.getEncoding();

		this.clearPluginFilters();

		this.clearPluginServlets();

		this.filterService = new FutureAcceptor(this, futureAcceptorServiceFilter);
		
		File temp = new File(bundle.getClassPath() + appPath);

		this.appLocalAddres = temp.getCanonicalPath() + "/";

		LoggerUtil.prettyNIOServerLog(logger, "application path      :{ {} }", appLocalAddres);

		LifeCycleUtil.start(sequence);

		LifeCycleUtil.start(filterService);

		this.roleManager.initialize(this, null);
		this.loginCenter.initialize(this, null);

		this.acceptorServiceLoader = filterService.getFutureAcceptorServiceLoader();
		this.acceptorServiceLoader.listen(services);
		this.context.setSessionAttachmentSize(filterService.getPluginContexts().length);
	}

	public void addSessionEventListener(SocketSessionEventListener listener) {
		context.addSessionEventListener(listener);
	}

	@Override
	protected void doStop() throws Exception {
		LifeCycleUtil.start(sequence);
		LifeCycleUtil.stop(filterService);
		InitializeUtil.destroy(loginCenter, this, null);
		classLoader.unloadClassLoader();
		instance = null;
	}

	public String getAppLocalAddress() {
		return appLocalAddres;
	}

	public DynamicClassLoader getClassLoader() {
		return classLoader;
	}

	public ApplicationConfiguration getConfiguration() {
		return configuration;
	}

	public SocketChannelContext getContext() {
		return context;
	}

	public Charset getEncoding() {
		return encoding;
	}

	public FutureAcceptor getFilterService() {
		return filterService;
	}

	public LoginCenter getLoginCenter() {
		return loginCenter;
	}

	@SuppressWarnings("rawtypes")
	public PluginContext getPluginContext(Class clazz) {

		PluginContext[] pluginContexts = filterService.getPluginContexts();

		for (PluginContext context : pluginContexts) {

			if (context == null) {
				continue;
			}

			if (context.getClass().isAssignableFrom(clazz)) {
				return context;
			}
		}
		return null;
	}

	public List<FutureAcceptorFilter> getPluginFilters() {
		return pluginFilters;
	}

	public Map<String, FutureAcceptorService> getPluginServlets() {
		return pluginServlets;
	}

	public RoleManager getRoleManager() {
		return roleManager;
	}

	private void clearPluginServlets() {
		pluginServlets.clear();
		putPluginServices(getAppRedeployService());
		putPluginServices(new SystemStopServerServlet());

	}

	private void putPluginServices(FutureAcceptorService service) {
		pluginServlets.put(service.getServiceName(), service);
	}

	private void clearPluginFilters() {
		pluginFilters.clear();
	}

	// FIXME 考虑部署失败后如何再次部署
	// FIXME redeploy roleManager
	// FIXME redeploy loginCenter
	// FIXME keep http session
	public synchronized boolean redeploy() {

		LoggerUtil.prettyNIOServerLog(logger, "//**********************  开始卸载服务  **********************//");

		LifeCycleUtil.stop(sequence);

		LifeCycleUtil.stop(filterService);

		clearPluginFilters();

		clearPluginServlets();

		classLoader.unloadClassLoader();

		LoggerUtil.prettyNIOServerLog(logger, "//**********************  卸载服务完成  **********************//\n");

		try {

			// FIXME 重新加载configuration
			LoggerUtil.prettyNIOServerLog(logger, "//**********************  开始加载服务  **********************//");

			this.classLoader = new DynamicClassLoader();

			LifeCycleUtil.start(sequence);

			LifeCycleUtil.start(filterService);

			LoggerUtil.prettyNIOServerLog(logger, "//**********************  加载服务完成  **********************//\n");

			return true;

		} catch (Exception e) {

			classLoader.unloadClassLoader();

			LoggerUtil.prettyNIOServerLog(logger, "//**********************  加载服务失败  **********************//\n");

			logger.info(e.getMessage(), e);

			return false;
		}
	}

	public void setContext(SocketChannelContext context) {
		this.context = context;
	}

	public void setLoginCenter(LoginCenter loginCenter) {

		if (loginCenter == null) {
			throw new IllegalArgumentException("null");
		}

		if (this.loginCenter.getClass() != AuthorityLoginCenter.class) {
			// FIXME 这里是否只能设置一次
			// throw new IllegalArgumentException("already setted");
		}

		this.loginCenter = loginCenter;
	}

	public void listen(String serviceName, FutureAcceptorService service) {

		if (isRunning()) {
			throw new IllegalStateException("listen before start");
		}

		this.services.put(serviceName, service);
	}

	public Sequence getSequence() {
		return sequence;
	}

	public FutureAcceptorService getAppRedeployService() {
		return appRedeployService;
	}

	public void setAppRedeployService(FutureAcceptorService appRedeployService) {
		this.appRedeployService = appRedeployService;
	}

	public void setServiceFilter(FutureAcceptorServiceFilter serviceFilter) {
		this.futureAcceptorServiceFilter = serviceFilter;
	}

}
