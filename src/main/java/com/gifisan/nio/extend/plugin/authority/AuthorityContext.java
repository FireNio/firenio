package com.gifisan.nio.extend.plugin.authority;

import java.util.Map;

import com.gifisan.nio.component.Session;
import com.gifisan.nio.extend.AbstractPluginContext;
import com.gifisan.nio.extend.ApplicationContext;
import com.gifisan.nio.extend.configuration.Configuration;
import com.gifisan.nio.extend.service.FutureAcceptorService;
import com.likemessage.server.LMLoginCenter;

public class AuthorityContext extends AbstractPluginContext {
	
	private static AuthorityContext instance = null;
	
	public static AuthorityContext getInstance(){
		return instance;
	}

	public void configFutureAcceptor(Map<String, FutureAcceptorService> acceptors) {
		acceptors.put(SYSTEMAuthorityServlet.SERVICE_NAME, new SYSTEMAuthorityServlet());
	}

	public void initialize(ApplicationContext context, Configuration config) throws Exception {

		context.setLoginCenter(new LMLoginCenter());
		
		context.addSessionEventListener(new AuthoritySEListener());
		
		instance = this;
	}
	
	public AuthoritySessionAttachment getSessionAttachment(Session session){
		
		return (AuthoritySessionAttachment) session.getAttachment(this);
	}

}
