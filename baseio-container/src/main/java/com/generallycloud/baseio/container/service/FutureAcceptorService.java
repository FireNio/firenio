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

import com.generallycloud.baseio.common.Logger;
import com.generallycloud.baseio.common.LoggerFactory;
import com.generallycloud.baseio.common.StringUtil;
import com.generallycloud.baseio.component.IoEventHandle;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.container.ApplicationContext;
import com.generallycloud.baseio.container.Initializeable;
import com.generallycloud.baseio.container.InitializeableImpl;
import com.generallycloud.baseio.container.configuration.Configuration;
import com.generallycloud.baseio.protocol.ReadFuture;

public abstract class FutureAcceptorService extends InitializeableImpl implements Initializeable, IoEventHandle {

	private Logger		logger		= LoggerFactory.getLogger(getClass());

	private String		serviceName	= null;

	@Override
	public void initialize(ApplicationContext context, Configuration config) throws Exception {

	}

	@Override
	public void futureSent(SocketSession session, ReadFuture future) {

	}

	@Override
	public void exceptionCaught(SocketSession session, ReadFuture future, Exception cause, IoEventState state) {
		logger.error(cause.getMessage(), cause);
	}

	@Override
	public String toString() {

		String serviceName = this.serviceName;

		if (serviceName == null) {

			Configuration configuration = this.getConfig();

			if (configuration != null) {
				serviceName = configuration.getParameter("service-name");
			}

			if (serviceName == null) {
				serviceName = "unknow";
			}
		}

		return "(service-name:" + serviceName + "@class:" + this.getClass().getName() + ")";
	}
	
	public String getServiceName() {
		return serviceName;
	}
	
	public void setServiceName(String serviceName) {
		
		if (StringUtil.isNullOrBlank(serviceName)) {
			throw new IllegalArgumentException("null future name");
		}
		
		this.serviceName = serviceName;
	}

}
