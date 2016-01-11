package com.gifisan.mtp.servlet;

import com.gifisan.mtp.AbstractLifeCycle;
import com.gifisan.mtp.LifeCycle;
import com.gifisan.mtp.component.ServletConfig;
import com.gifisan.mtp.server.ServletAcceptor;
import com.gifisan.mtp.server.ServletContext;

public abstract class GenericServlet extends AbstractLifeCycle implements LifeCycle, ServletAcceptor{

	private ServletContext context = null;
	
	private ServletConfig config = null;
	
	public ServletConfig getConfig() {
		return this.config;
	}
	
	public void setConfig(ServletConfig config) {
		this.config = config;
	}
	
	protected void doStart() throws Exception {
		this.initialize(context,config);
		
	}
	
	protected void doStop() throws Exception {
		this.destroy(context,config);
	}
	
	public void setServletContext(ServletContext context){
		this.context = context;
	}

	public abstract void initialize(ServletContext context,ServletConfig config) throws Exception ;
	
	public abstract void destroy(ServletContext context,ServletConfig config) throws Exception ;
	
}
