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

import com.generallycloud.baseio.AbstractLifeCycle;
import com.generallycloud.baseio.LifeCycle;
import com.generallycloud.baseio.LifeCycleUtil;
import com.generallycloud.baseio.common.Logger;
import com.generallycloud.baseio.common.LoggerFactory;
import com.generallycloud.baseio.component.IoEventHandle;
import com.generallycloud.baseio.component.IoEventHandle.IoEventState;
import com.generallycloud.baseio.component.ReadFutureAcceptor;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.container.ApplicationContext;
import com.generallycloud.baseio.container.DynamicClassLoader;
import com.generallycloud.baseio.container.PluginContext;
import com.generallycloud.baseio.protocol.ReadFuture;

//FIXME exception
public final class FutureAcceptor extends AbstractLifeCycle
		implements LifeCycle, ReadFutureAcceptor {

	private volatile boolean			deploying = true;
	private ApplicationContext			context;
	private FutureAcceptorFilterLoader		filterLoader;
	private PluginLoader				pluginLoader;
	private FutureAcceptorFilterWrapper	rootFilter;
	private FutureAcceptorServiceFilter	serviceFilter;
	private FutureAcceptorService			appRedeployService;
	private Logger						logger	= LoggerFactory.getLogger(getClass());

	public FutureAcceptor(ApplicationContext context, FutureAcceptorServiceFilter serviceFilter) {
		this.context = context;
		this.serviceFilter = serviceFilter;
	}

	@Override
	public void accept(SocketSession session, ReadFuture future) throws Exception {

		if (deploying) {

			appRedeployService.accept(session, future);

			return;
		}
		
		try {

			rootFilter.accept(session, future);

		} catch (Exception e) {

			logger.errorDebug(e);

			IoEventHandle eventHandle = future.getIoEventHandle();

			eventHandle.exceptionCaught(session, future, e, IoEventState.HANDLE);
		}
	}

	@Override
	protected void doStart() throws Exception {

		DynamicClassLoader classLoader = context.getClassLoader();

		context.getApplicationExtLoader().loadExts(context, classLoader);

		this.appRedeployService = context.getAppRedeployService();

		if (pluginLoader == null) {
			this.pluginLoader = new PluginLoader(context);
		}

		if (filterLoader == null) {
			this.filterLoader = new FutureAcceptorFilterLoader(context, serviceFilter);
		}

		LifeCycleUtil.start(pluginLoader);

		LifeCycleUtil.start(filterLoader);

		this.rootFilter = filterLoader.getRootFilter();

		this.deploying = false;
	}

	@Override
	protected void doStop() throws Exception {
		this.deploying = true;
		LifeCycleUtil.stop(filterLoader);
		LifeCycleUtil.stop(pluginLoader);
	}

	public FutureAcceptorServiceLoader getFutureAcceptorServiceLoader() {
		return filterLoader.getFutureAcceptorServiceLoader();
	}

	public PluginContext[] getPluginContexts() {
		return pluginLoader.getPluginContexts();
	}

}
