package com.generallycloud.nio.extend.plugin.authority;

import java.util.List;
import java.util.Map;

import com.generallycloud.nio.component.Session;
import com.generallycloud.nio.extend.AbstractPluginContext;
import com.generallycloud.nio.extend.ApplicationContext;
import com.generallycloud.nio.extend.configuration.Configuration;
import com.generallycloud.nio.extend.implementation.AuthorityFilter;
import com.generallycloud.nio.extend.security.AuthorityLoginCenter;
import com.generallycloud.nio.extend.service.FutureAcceptorFilter;
import com.generallycloud.nio.extend.service.FutureAcceptorService;

public class AuthorityContext extends AbstractPluginContext {
	
	private static AuthorityContext instance = null;
	
	public static AuthorityContext getInstance(){
		return instance;
	}

	public void configFutureAcceptor(Map<String, FutureAcceptorService> acceptors) {
		acceptors.put(SYSTEMAuthorityServlet.SERVICE_NAME, new SYSTEMAuthorityServlet());
	}
	
	public void configFutureAcceptorFilter(List<FutureAcceptorFilter> filters) {
		
		AuthorityFilter authorityFilter = new AuthorityFilter();
		authorityFilter.setSortIndex(0);
		
		filters.add(authorityFilter);
	}

	public void initialize(ApplicationContext context, Configuration config) throws Exception {

		context.setLoginCenter(new AuthorityLoginCenter());
		
		context.addSessionEventListener(new AuthoritySEListener());
		
		instance = this;
	}
	
	public AuthoritySessionAttachment getSessionAttachment(Session session){
		
		return (AuthoritySessionAttachment) session.getAttachment(this.getPluginIndex());
	}

}
