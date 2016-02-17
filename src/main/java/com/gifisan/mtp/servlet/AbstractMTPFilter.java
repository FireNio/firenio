package com.gifisan.mtp.servlet;

import com.gifisan.mtp.component.Configuration;
import com.gifisan.mtp.server.ServerContext;

public abstract class AbstractMTPFilter implements MTPFilter{

	public void initialize(ServerContext context, Configuration config) throws Exception {
		
	}

	public void destroy(ServerContext context, Configuration config) throws Exception {
		
	}

	public void onPreDeploy(ServerContext context, Configuration config) throws Exception {
		this.initialize(context, config);
		
	}

	public void onSubDeploy(ServerContext context, Configuration config) throws Exception {
		this.destroy(context, config);
		
	}

	

	
	
	
}
