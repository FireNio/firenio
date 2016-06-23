package com.gifisan.nio.server.service;

import com.gifisan.nio.component.ApplicationContext;
import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.component.HotDeploy;
import com.gifisan.nio.component.Initializeable;
import com.gifisan.nio.component.InitializeableImpl;
import com.gifisan.nio.component.ReadFutureAcceptor;

public abstract class FutureAcceptorFilter extends InitializeableImpl implements Initializeable, HotDeploy, ReadFutureAcceptor {
	
	public void initialize(ApplicationContext context, Configuration config) throws Exception {
		
	}

	public void destroy(ApplicationContext context, Configuration config) throws Exception {
		
	}
	
	public void prepare(ApplicationContext context, Configuration config) throws Exception {
		this.initialize(context, config);
	}

	public void unload(ApplicationContext context, Configuration config) throws Exception {
		this.destroy(context, config);
	}
}
