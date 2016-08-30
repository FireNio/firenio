package com.gifisan.nio.extend.plugin.http;

import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.component.concurrent.UniqueThread;
import com.gifisan.nio.extend.AbstractPluginContext;
import com.gifisan.nio.extend.ApplicationContext;
import com.gifisan.nio.extend.configuration.Configuration;

public class HttpContext extends AbstractPluginContext {

	private static HttpContext	instance			;

	private HttpSessionFactory	httpSessionFactory	= new HttpSessionFactory();

	private UniqueThread		taskExecutorThread	;

	public static HttpContext getInstance() {
		return instance;
	}

	public void initialize(ApplicationContext context, Configuration config) throws Exception {

		this.taskExecutorThread = new UniqueThread(httpSessionFactory, "HTTPSession-Manager");
		
		this.taskExecutorThread.start();

		instance = this;
	}

	public void destroy(ApplicationContext context, Configuration config) throws Exception {

		LifeCycleUtil.stop(taskExecutorThread);

		super.destroy(context, config);
	}

	public HttpSessionFactory getHttpSessionFactory() {
		return httpSessionFactory;
	}

}
