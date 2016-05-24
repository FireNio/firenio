package com.gifisan.nio.component;

import java.util.concurrent.atomic.AtomicInteger;

import com.gifisan.nio.common.ClassUtil;
import com.gifisan.nio.common.StringUtil;
import com.gifisan.nio.server.IOSession;
import com.gifisan.nio.server.ServerContext;

public abstract class AbstractPluginContext extends InitializeableImpl implements PluginContext {

	protected LoginCenter		loginCenter	= null;
	private int				pluginIndex	= 0;
	private static AtomicInteger	_index		= new AtomicInteger();

	protected AbstractPluginContext() {
		this.pluginIndex = _index.getAndIncrement();
	}

	public int getPluginIndex() {
		return pluginIndex;
	}

	public LoginCenter getLoginCenter() {
		return loginCenter;
	}

	public boolean isLogined(IOSession session) {

		return this.loginCenter.isLogined(session);
	}

	public void initialize(ServerContext context, Configuration config) throws Exception {

		String loginCenter = config.getParameter("login-center");

		if (StringUtil.isNullOrBlank(loginCenter)) {
			this.loginCenter = new FixedPluginLoginCenter(this);
		} else {
			Class clazz = this.getClass().getClassLoader().loadClass(loginCenter);

			this.loginCenter = (LoginCenter) ClassUtil.newInstance(clazz);
		}

		this.loginCenter.initialize(context, config);
	}

	public void destroy(ServerContext context, Configuration config) throws Exception {
		loginCenter.destroy(context, config);
	}

	// FIXME you wen ti
	public void prepare(ServerContext context, Configuration config) throws Exception {
		this.initialize(context, config);
	}

	// FIXME you wen ti
	public void unload(ServerContext context, Configuration config) throws Exception {
		this.destroy(context, config);
	}

}
