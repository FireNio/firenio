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
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import com.generallycloud.baseio.AbstractLifeCycle;
import com.generallycloud.baseio.LifeCycleUtil;
import com.generallycloud.baseio.common.FileUtil;
import com.generallycloud.baseio.common.Logger;
import com.generallycloud.baseio.common.LoggerFactory;
import com.generallycloud.baseio.common.LoggerUtil;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.ExceptionCaughtHandle;
import com.generallycloud.baseio.component.LoggerExceptionCaughtHandle;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSessionEventListener;
import com.generallycloud.baseio.container.authority.AuthorityLoginCenter;
import com.generallycloud.baseio.container.authority.RoleManager;
import com.generallycloud.baseio.container.configuration.ApplicationConfiguration;
import com.generallycloud.baseio.container.configuration.ApplicationConfigurationLoader;
import com.generallycloud.baseio.container.configuration.FileSystemACLoader;
import com.generallycloud.baseio.container.implementation.SystemRedeployServlet;
import com.generallycloud.baseio.container.implementation.SystemStopServerServlet;
import com.generallycloud.baseio.container.service.FutureAcceptor;
import com.generallycloud.baseio.container.service.FutureAcceptorFilter;
import com.generallycloud.baseio.container.service.FutureAcceptorService;
import com.generallycloud.baseio.container.service.FutureAcceptorServiceFilter;
import com.generallycloud.baseio.container.service.FutureAcceptorServiceLoader;

public class ApplicationContext extends AbstractLifeCycle {

	private static ApplicationContext instance;

	public static ApplicationContext getInstance() {
		return instance;
	}

	private boolean						deployModel;
	private boolean						redeploying;
	private String							appLocalAddres;
	private String							rootLocalAddress;
	private URLDynamicClassLoader				classLoader	;
	private ApplicationConfiguration			configuration;
	private SocketChannelContext				channelContext;
	private Charset						encoding;
	private Set<String> 					blackIPs;
	private FutureAcceptorService				appRedeployService;
	private FutureAcceptor					filterService	;
	private ApplicationExtLoader				applicationExtLoader;
	private ApplicationConfigurationLoader		acLoader;
	private AtomicInteger					pluginIndex;
	private ExceptionCaughtHandle				exceptionCaughtHandle;
	private Logger							logger		= LoggerFactory.getLogger(getClass());
	private LoginCenter						loginCenter	= new AuthorityLoginCenter();
	private List<FutureAcceptorFilter>			pluginFilters	= new ArrayList<FutureAcceptorFilter>();
	private Map<String, FutureAcceptorService>	pluginServlets	= new HashMap<String, FutureAcceptorService>();
	private RoleManager						roleManager	= new RoleManager();
	private FutureAcceptorServiceLoader		acceptorServiceLoader;
	private FutureAcceptorServiceFilter		futureAcceptorServiceFilter;
	private Map<String, FutureAcceptorService>	services		= new LinkedHashMap<String, FutureAcceptorService>();

	public ApplicationContext(String rootLocalAddress) {
		this(rootLocalAddress,null);
	}
	
	public ApplicationContext(ApplicationConfiguration configuration) {
		this(FileUtil.getCurrentPath(),configuration);
	}
	
	public ApplicationContext(String rootLocalAddress,ApplicationConfiguration configuration) {
		this.rootLocalAddress = rootLocalAddress;
		this.configuration = configuration;
	}

	@Override
	protected void doStart() throws Exception {

		if (channelContext == null) {
			throw new IllegalArgumentException("null nio context");
		}
		
		if (StringUtil.isNullOrBlank(rootLocalAddress)) {
			throw new IllegalArgumentException("rootLocalAddress");
		}
		
		if (futureAcceptorServiceFilter == null) {
			this.futureAcceptorServiceFilter = new FutureAcceptorServiceFilter();
		}

		if (appRedeployService == null) {
			appRedeployService = new SystemRedeployServlet();
		}
		
		if (acLoader == null) {
			acLoader = new FileSystemACLoader();
		}
		
		if (exceptionCaughtHandle == null) {
			exceptionCaughtHandle = new LoggerExceptionCaughtHandle();
		}
		
		instance = this;
		
		this.rootLocalAddress = FileUtil.getPrettyPath(rootLocalAddress);

		this.encoding = channelContext.getEncoding();

		this.clearPluginFilters();

		this.clearPluginServlets();

		this.filterService = new FutureAcceptor(this, futureAcceptorServiceFilter);
		
		this.appLocalAddres = FileUtil.getPrettyPath(getRootLocalAddress() + "app");
		
		LoggerUtil.prettyNIOServerLog(logger, "application path      :{ {} }", appLocalAddres);

		this.initializeApplicationContext();
		
		this.channelContext.setSessionAttachmentSize(filterService.getPluginContexts().length);
	}
	
	private URLDynamicClassLoader newClassLoader() throws IOException{
		
		URLDynamicClassLoader classLoader = new URLDynamicClassLoader(getClass().getClassLoader());
		
		classLoader.addMatchStartWith("com/generallycloud/baseio/");
		
		if (deployModel) {
			
			classLoader.scan(new File(getRootLocalAddress()+"/lib"));
			
			classLoader.scan(new File(getRootLocalAddress()+"/conf"));
		}else{
			
			classLoader.addExcludePath("/app");
			
			classLoader.scan(new File(getRootLocalAddress()));
		}
		
		return classLoader;
	}
	
	private void initializeApplicationContext() throws Exception{
		
		this.pluginIndex = new AtomicInteger();
		
		this.classLoader = newClassLoader();
		
		this.configuration = acLoader.loadConfiguration(classLoader);

		LifeCycleUtil.start(filterService);

		this.roleManager.initialize(this, null);
		this.loginCenter.initialize(this, null);
		
		this.acceptorServiceLoader = filterService.getFutureAcceptorServiceLoader();
		this.acceptorServiceLoader.listen(services);
	}
	
	private void destroyApplicationContext(){
		
		LifeCycleUtil.stop(filterService);
		
		InitializeUtil.destroy(loginCenter, this,null);
		InitializeUtil.destroy(roleManager, this,null);

		clearPluginFilters();

		clearPluginServlets();

		classLoader.unloadClassLoader();
	}

	public void addSessionEventListener(SocketSessionEventListener listener) {
		channelContext.addSessionEventListener(listener);
	}

	@Override
	protected void doStop() throws Exception {
		destroyApplicationContext();
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

	public SocketChannelContext getChannelContext() {
		return channelContext;
	}

	public Charset getEncoding() {
		return encoding;
	}

	protected FutureAcceptor getFilterService() {
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

		redeploying = true;
		
		destroyApplicationContext();

		LoggerUtil.prettyNIOServerLog(logger, "//**********************  卸载服务完成  **********************//\n");

		try {

			// FIXME 重新加载configuration
			LoggerUtil.prettyNIOServerLog(logger, "//**********************  开始加载服务  **********************//");

			initializeApplicationContext();
			
			redeploying = false;
			
			LoggerUtil.prettyNIOServerLog(logger, "//**********************  加载服务完成  **********************//\n");

			return true;

		} catch (Exception e) {

			classLoader.unloadClassLoader();
			
			redeploying = false;

			LoggerUtil.prettyNIOServerLog(logger, "//**********************  加载服务失败  **********************//\n");

			logger.info(e.getMessage(), e);

			return false;
		}
		
	}

	public void setChannelContext(SocketChannelContext context) {
		this.channelContext = context;
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

	public FutureAcceptorService getAppRedeployService() {
		return appRedeployService;
	}

	public void setAppRedeployService(FutureAcceptorService appRedeployService) {
		this.appRedeployService = appRedeployService;
	}

	public void setServiceFilter(FutureAcceptorServiceFilter serviceFilter) {
		this.futureAcceptorServiceFilter = serviceFilter;
	}

	public ApplicationExtLoader getApplicationExtLoader() {
		return applicationExtLoader;
	}

	public void setApplicationExtLoader(ApplicationExtLoader applicationExtLoader) {
		this.applicationExtLoader = applicationExtLoader;
	}
	
	public String getRootLocalAddress() {
		return rootLocalAddress;
	}
	
	public void setApplicationConfigurationLoader(ApplicationConfigurationLoader acLoader) {
		this.acLoader = acLoader;
	}
	
	/**
	 * @return the pluginIndex
	 */
	public AtomicInteger getPluginIndex() {
		return pluginIndex;
	}
	
	/**
	 * @return the redeploying
	 */
	public boolean isRedeploying() {
		return redeploying;
	}

	public ExceptionCaughtHandle getExceptionCaughtHandle() {
		return exceptionCaughtHandle;
	}

	public void setExceptionCaughtHandle(ExceptionCaughtHandle exceptionCaughtHandle) {
		this.exceptionCaughtHandle = exceptionCaughtHandle;
	}

	public Set<String> getBlackIPs() {
		return blackIPs;
	}

	public void setBlackIPs(Set<String> blackIPs) {
		this.blackIPs = blackIPs;
	}
	
	public void setDeployModel(boolean deployModel) {
		this.deployModel = deployModel;
	}
	
	public boolean isDeployModel() {
		return deployModel;
	}
	
}
