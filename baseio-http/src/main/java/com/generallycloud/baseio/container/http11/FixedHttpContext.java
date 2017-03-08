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
package com.generallycloud.baseio.container.http11;

import com.generallycloud.baseio.codec.http11.HttpContext;
import com.generallycloud.baseio.codec.http11.future.WebSocketSEListener;
import com.generallycloud.baseio.common.LifeCycleUtil;
import com.generallycloud.baseio.container.AbstractPluginContext;
import com.generallycloud.baseio.container.ApplicationContext;
import com.generallycloud.baseio.container.configuration.Configuration;

public class FixedHttpContext extends AbstractPluginContext{
	
	private HttpContext httpContext = new HttpContext();

	@Override
	public void initialize(ApplicationContext context, Configuration config) throws Exception {
		
		this.httpContext.start();
		
		context.getContext().addSessionEventListener(new WebSocketSEListener());
	}

	@Override
	public void destroy(ApplicationContext context, Configuration config) throws Exception {
		
		LifeCycleUtil.stop(httpContext);
		
		super.destroy(context, config);
	}
}
