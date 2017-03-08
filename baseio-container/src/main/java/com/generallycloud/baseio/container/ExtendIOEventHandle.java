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

import com.generallycloud.baseio.common.Logger;
import com.generallycloud.baseio.common.LoggerFactory;
import com.generallycloud.baseio.component.IoEventHandleAdaptor;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.container.service.FutureAcceptor;
import com.generallycloud.baseio.live.LifeCycleUtil;
import com.generallycloud.baseio.protocol.ReadFuture;

public class ExtendIOEventHandle extends IoEventHandleAdaptor {

	private ApplicationContext	applicationContext;
	private FutureAcceptor		filterService;
	private Logger				logger	= LoggerFactory.getLogger(ExtendIOEventHandle.class);

	public ExtendIOEventHandle(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public void accept(SocketSession session, ReadFuture future) {

		try {

			filterService.accept(session, future);

		} catch (Exception e) {

			logger.error(e.getMessage(), e);

			exceptionCaught(session, future, e, IoEventState.HANDLE);
		}
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	@Override
	protected void doStart() throws Exception {

		LifeCycleUtil.start(applicationContext);

		this.filterService = applicationContext.getFilterService();

		super.doStart();
	}

	@Override
	protected void doStop() throws Exception {
		LifeCycleUtil.stop(applicationContext);

		super.doStop();
	}

}
