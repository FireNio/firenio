package com.generallycloud.nio.codec.http11;

import com.generallycloud.nio.AbstractLifeCycle;
import com.generallycloud.nio.common.LifeCycleUtil;

public class HttpContext extends AbstractLifeCycle {

	private static HttpContext	instance;

	private HttpSessionManager	httpSessionManager	= new HttpSessionManager();

	public static HttpContext getInstance() {
		return instance;
	}

	@Override
	protected void doStart() throws Exception {
		
		this.httpSessionManager.startup("HTTPSession-Manager");

		instance = this;
	}

	@Override
	protected void doStop() throws Exception {
		LifeCycleUtil.stop(httpSessionManager);
	}

	public HttpSessionManager getHttpSessionManager() {
		return httpSessionManager;
	}

}
