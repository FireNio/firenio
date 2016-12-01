package com.generallycloud.nio.container.authority;

import java.util.List;
import java.util.Map;

import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.AbstractPluginContext;
import com.generallycloud.nio.container.ApplicationContext;
import com.generallycloud.nio.container.configuration.Configuration;
import com.generallycloud.nio.container.implementation.AuthorityFilter;
import com.generallycloud.nio.container.service.FutureAcceptorFilter;
import com.generallycloud.nio.container.service.FutureAcceptorService;

public abstract class AuthorityContext extends AbstractPluginContext {
	
	private static AuthorityContext instance = null;
	
	public static AuthorityContext getInstance(){
		return instance;
	}

	public void configFutureAcceptor(Map<String, FutureAcceptorService> acceptors) {
		acceptors.put(SYSTEMAuthorityServlet.SERVICE_NAME, createSYSTEMAuthorityServlet());
	}
	
	protected abstract SYSTEMAuthorityServlet createSYSTEMAuthorityServlet();
	
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
	
	public AuthoritySessionAttachment getSessionAttachment(SocketSession session){
		
		return (AuthoritySessionAttachment) session.getAttachment(this.getPluginIndex());
	}

}
