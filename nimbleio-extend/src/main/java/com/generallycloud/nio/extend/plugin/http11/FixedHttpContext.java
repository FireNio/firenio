package com.generallycloud.nio.extend.plugin.http11;

import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.component.Configuration;
import com.generallycloud.nio.component.protocol.http11.HttpContext;
import com.generallycloud.nio.extend.AbstractPluginContext;
import com.generallycloud.nio.extend.ApplicationContext;

public class FixedHttpContext extends AbstractPluginContext{
	
	private HttpContext httpContext = new HttpContext();

	public void initialize(ApplicationContext context, Configuration config) throws Exception {
		this.httpContext.start();
	}

	public void destroy(ApplicationContext context, Configuration config) throws Exception {
		
		LifeCycleUtil.stop(httpContext);
		
		super.destroy(context, config);
	}
}
