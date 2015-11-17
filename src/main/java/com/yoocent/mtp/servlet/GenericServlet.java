package com.yoocent.mtp.servlet;

import com.yoocent.mtp.AbstractLifeCycle;
import com.yoocent.mtp.LifeCycle;
import com.yoocent.mtp.component.ServletConfig;
import com.yoocent.mtp.server.ServletAcceptAble;
import com.yoocent.mtp.server.context.ServletContext;

public abstract class GenericServlet extends AbstractLifeCycle implements LifeCycle, ServletAcceptAble{

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
