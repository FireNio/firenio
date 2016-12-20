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
package com.generallycloud.nio.container.authority;

import java.util.List;
import java.util.Map;

import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.AbstractPluginContext;
import com.generallycloud.nio.container.ApplicationContext;
import com.generallycloud.nio.container.configuration.Configuration;
import com.generallycloud.nio.container.service.FutureAcceptorFilter;
import com.generallycloud.nio.container.service.FutureAcceptorService;

public class AuthorityContext extends AbstractPluginContext {

	private static AuthorityContext	instance	= null;

	public static AuthorityContext getInstance() {
		return instance;
	}

	@Override
	public void configFutureAcceptor(Map<String, FutureAcceptorService> acceptors) {
		acceptors.put("/login", new SYSTEMAuthorityServlet());
	}

	@Override
	public void configFutureAcceptorFilter(List<FutureAcceptorFilter> filters) {

		AuthorityFilter authorityFilter = new AuthorityFilter();
		
		authorityFilter.setSortIndex(0);

		filters.add(authorityFilter);
	}

	@Override
	public void initialize(ApplicationContext context, Configuration config) throws Exception {

		context.setLoginCenter(new AuthorityLoginCenter());

		context.addSessionEventListener(new AuthoritySEListener());
		
		instance = this;
	}

	public AuthoritySessionAttachment getSessionAttachment(SocketSession session) {

		return (AuthoritySessionAttachment) session.getAttachment(this.getPluginIndex());
	}

}
