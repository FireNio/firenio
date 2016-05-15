package com.gifisan.nio.component;

import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.IOSession;

public class FixedPluginLoginCenter extends PluginLoginCenter implements LoginCenter {

	public FixedPluginLoginCenter(PluginContext context) {
		super(context);
	}

	public boolean login(IOSession session, ServerReadFuture future) {

		return session.getLoginCenter().login(session, future);
	}

	public boolean isLogined(IOSession session) {
		return session.getLoginCenter().isLogined(session);
	}

	public void logout(IOSession session) {
		session.getLoginCenter().logout(session);
	}

	public boolean isValidate(IOSession session, ServerReadFuture future) {

		return session.getLoginCenter().isValidate(session, future);
	}

}
