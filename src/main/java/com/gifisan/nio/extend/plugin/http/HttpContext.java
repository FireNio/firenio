package com.gifisan.nio.extend.plugin.http;

import com.gifisan.nio.common.LifeCycleUtil;
import com.gifisan.nio.component.concurrent.TaskExecutor;
import com.gifisan.nio.component.concurrent.UniqueThread;
import com.gifisan.nio.extend.AbstractPluginContext;
import com.gifisan.nio.extend.ApplicationContext;
import com.gifisan.nio.extend.configuration.Configuration;

public class HttpContext extends AbstractPluginContext {

	private static HttpContext	instance			= null;

	private HttpSessionFactory	httpSessionFactory	= new HttpSessionFactory();

	private TaskExecutor		taskExecutor		= new TaskExecutor(httpSessionFactory, 1000 * 60 * 60);

	private UniqueThread		taskExecutorThread	= new UniqueThread();

	public static HttpContext getInstance() {
		return instance;
	}

	public void initialize(ApplicationContext context, Configuration config) throws Exception {

		this.taskExecutorThread.start(taskExecutor, "HTTPSession-Manager");

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
