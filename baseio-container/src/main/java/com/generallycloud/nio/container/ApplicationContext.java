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
package com.generallycloud.nio.container;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.generallycloud.nio.AbstractLifeCycle;
import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.LoggerUtil;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.component.SocketChannelContext;
import com.generallycloud.nio.component.SocketSessionEventListener;
import com.generallycloud.nio.container.authority.AuthorityLoginCenter;
import com.generallycloud.nio.container.authority.RoleManager;
import com.generallycloud.nio.container.configuration.ApplicationConfiguration;
import com.generallycloud.nio.container.service.FutureAcceptor;
import com.generallycloud.nio.container.service.FutureAcceptorFilter;
import com.generallycloud.nio.container.service.FutureAcceptorService;
import com.generallycloud.nio.container.service.FutureAcceptorServiceLoader;

public class ApplicationContext extends AbstractLifeCycle {

	private static ApplicationContext	instance;

	public static ApplicationContext getInstance() {
		return instance;
	}

	private String							basePath			= "";
	private String							appPath			= "app/";
	private String							appLocalAddres;
	private Sequence						sequence			= new Sequence();
	private DynamicClassLoader				classLoader		= new DynamicClassLoader();
	private ApplicationConfiguration			configuration;
	private SocketChannelContext						context;
	private Charset						encoding			;
	private FutureAcceptor					filterService;
	private Logger							logger			= LoggerFactory
																.getLogger(ApplicationContext.class);
	private LoginCenter						loginCenter		= new AuthorityLoginCenter();
	private List<FutureAcceptorFilter>			pluginFilters		= new ArrayList<FutureAcceptorFilter>();
	private Map<String, FutureAcceptorService>	pluginServlets		= new HashMap<String, FutureAcceptorService>();
	private RoleManager						roleManager		= new RoleManager();
	private FutureAcceptorServiceLoader		acceptorServiceLoader;
	private Map<String, FutureAcceptorService>	services			= new LinkedHashMap<String, FutureAcceptorService>();

	public ApplicationContext(ApplicationConfiguration configuration, String basePath) {
		if (basePath == null) {
			basePath = "";
		}
		this.configuration = configuration;
		this.basePath = basePath;
	}

	protected ApplicationContext(ApplicationConfiguration configuration) {
		this(configuration, "");
	}

	@Override
	protected void doStart() throws Exception {

		if (context == null) {
			throw new IllegalArgumentException("null nio context");
		}

		instance = this;

		SharedBundle bundle = SharedBundle.instance();

		this.filterService = new FutureAcceptor(this);
		
		this.filterService.setClassLoader(classLoader);

		this.encoding = context.getEncoding();
		
		File temp = new File( bundle.getClassPath() + basePath + "/" + appPath);
		
		this.appLocalAddres = temp.getCanonicalPath() + "/";

		LoggerUtil.prettyNIOServerLog(logger, "工作目录           ：{ {} }", appLocalAddres);

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
		LifeCycleUtil.stop(filterService);
		InitializeUtil.destroy(loginCenter, this, null);
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

	public boolean redeploy() {
		
		LifeCycleUtil.stop(filterService);
		
		DynamicClassLoader classLoader = new DynamicClassLoader();

		try {

			// FIXME 重新加载configuration
			
			filterService.setClassLoader(classLoader);
			
			LifeCycleUtil.start(filterService);
			
		} catch (Exception e) {
			logger.info(e.getMessage(), e);
			return false;
		}


		this.classLoader = classLoader;

		return true;
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

}
