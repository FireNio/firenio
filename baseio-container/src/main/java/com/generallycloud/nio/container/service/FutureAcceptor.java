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

import java.io.IOException;

import com.generallycloud.nio.AbstractLifeCycle;
import com.generallycloud.nio.LifeCycle;
import com.generallycloud.nio.Linkable;
import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.component.IoEventHandle;
import com.generallycloud.nio.component.IoEventHandle.IoEventState;
import com.generallycloud.nio.component.ReadFutureAcceptor;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.ApplicationContext;
import com.generallycloud.nio.container.DynamicClassLoader;
import com.generallycloud.nio.container.PluginContext;
import com.generallycloud.nio.protocol.ReadFuture;

//FIXME exception
public final class FutureAcceptor extends AbstractLifeCycle implements LifeCycle, ReadFutureAcceptor {

	private volatile boolean			deploying;
	private DynamicClassLoader			classLoader;
	private ApplicationContext			context;
	private FutureAcceptorFilterLoader		filterLoader;
	private PluginLoader				pluginLoader;
	private Linkable<FutureAcceptorFilter>	rootFilter;
	private FutureAcceptorServiceFilter	serviceFilter;
	private FutureAcceptorService			appRedeployService;
	private Logger						logger	= LoggerFactory.getLogger(FutureAcceptor.class);

	public FutureAcceptor(ApplicationContext context, FutureAcceptorServiceFilter serviceFilter) {
		this.context = context;
		this.serviceFilter = serviceFilter;
	}

	private void accept(Linkable<FutureAcceptorFilter> filter, SocketSession session, ReadFuture future) {

		try {

			FutureAcceptorFilter acceptorFilter = filter.getValue();

			future.setIOEventHandle(acceptorFilter);

			acceptorFilter.accept(session, future);

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			IoEventHandle eventHandle = future.getIOEventHandle();

			eventHandle.exceptionCaught(session, future, e, IoEventState.HANDLE);
		}
	}

	@Override
	public void accept(SocketSession session, ReadFuture future) throws IOException {

		try {

			if (deploying) {

				appRedeployService.accept(session, future);

				return;
			}

			accept(rootFilter, session, future);

		} catch (Throwable e) {

			logger.error(e.getMessage(), e);
		}
	}

	@Override
	protected void doStart() throws Exception {

		this.classLoader = context.getClassLoader();

		this.classLoader.scan(context.getAppLocalAddress());

		this.appRedeployService = context.getAppRedeployService();

		this.serviceFilter.setClassLoader(classLoader);

		if (pluginLoader == null) {
			this.pluginLoader = new PluginLoader(context, classLoader);
		}

		if (filterLoader == null) {
			this.filterLoader = new FutureAcceptorFilterLoader(context, classLoader, serviceFilter);
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
