package com.generallycloud.nio.component.protocol.http11;

import com.generallycloud.nio.AbstractLifeCycle;
import com.generallycloud.nio.common.LifeCycleUtil;
import com.generallycloud.nio.component.concurrent.UniqueThread;

public class HttpContext extends AbstractLifeCycle {

	private static HttpContext	instance;

	private HttpSessionFactory	httpSessionFactory	= new HttpSessionFactory();

	private UniqueThread		taskExecutorThread;

	public static HttpContext getInstance() {
		return instance;
	}

	protected void doStart() throws Exception {
		this.taskExecutorThread = new UniqueThread(httpSessionFactory, "HTTPSession-Manager");

		this.taskExecutorThread.start();

		instance = this;
	}

	protected void doStop() throws Exception {
		LifeCycleUtil.stop(taskExecutorThread);
	}


	public HttpSessionFactory getHttpSessionFactory() {
		return httpSessionFactory;
	}

}
