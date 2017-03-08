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
package com.generallycloud.baseio.codec.http11;

import com.generallycloud.baseio.AbstractLifeCycle;
import com.generallycloud.baseio.common.LifeCycleUtil;

public class HttpContext extends AbstractLifeCycle {

	private static HttpContext	instance;

	private HttpSessionManager	httpSessionManager	= new HttpSessionManager();

	public static HttpContext getInstance() {
		return instance;
	}

	@Override
	protected void doStart() throws Exception {
		
		this.httpSessionManager.startup("HTTPSession-Manager");

		instance = this;
	}

	@Override
	protected void doStop() throws Exception {
		LifeCycleUtil.stop(httpSessionManager);
	}

	public HttpSessionManager getHttpSessionManager() {
		return httpSessionManager;
	}

}
