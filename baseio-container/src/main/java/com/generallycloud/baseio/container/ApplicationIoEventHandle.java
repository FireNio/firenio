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

import com.generallycloud.baseio.LifeCycleUtil;
import com.generallycloud.baseio.component.ExceptionCaughtHandle;
import com.generallycloud.baseio.component.IoEventHandleAdaptor;
import com.generallycloud.baseio.component.SocketChannelContext;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.container.service.FutureAcceptorContainer;
import com.generallycloud.baseio.protocol.Future;

public class ApplicationIoEventHandle extends IoEventHandleAdaptor {

	private ApplicationContext	applicationContext;
	private FutureAcceptorContainer		filterService;

	public ApplicationIoEventHandle(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public void accept(SocketSession session, Future future) throws Exception {
		filterService.accept(session, future);
	}

	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	@Override
	protected void initialize(SocketChannelContext context) throws Exception {

		ApplicationContext applicationContext = this.applicationContext;

		LifeCycleUtil.start(applicationContext);

		this.filterService = applicationContext.getFilterService();

		super.initialize(context);
	}

	@Override
	protected void destroy(SocketChannelContext context) throws Exception {

		LifeCycleUtil.stop(applicationContext);

		super.destroy(context);
	}
	
	@Override
	public void exceptionCaught(SocketSession session, Future future, Exception cause,
			IoEventState state) {
		
		ExceptionCaughtHandle exceptionCaughtHandle = applicationContext.getExceptionCaughtHandle();
		
		if (exceptionCaughtHandle == null) {
			return;
		}
		
		exceptionCaughtHandle.exceptionCaught(session, future, cause, state);
	}

}
