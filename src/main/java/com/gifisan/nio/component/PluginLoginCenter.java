package com.gifisan.nio.component;

import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.IOSession;
import com.gifisan.nio.server.ServerContext;
import com.gifisan.nio.server.ServerSession;

public class PluginLoginCenter extends InitializeableImpl implements LoginCenter {

	private String			username	= null;
	private String			password	= null;
	private PluginContext	context	= null;

	public PluginLoginCenter(PluginContext context) {
		this.context = context;
	}

	public boolean login(IOSession session, ServerReadFuture future) {

		ServerSession _Session = (ServerSession) session;

		if (isValidate(session, future)) {

			ActiveAuthority authority = _Session.getAuthority();

			if (authority == null) {

				authority = new DefaultAuthority();

				_Session.setAuthority(context, authority);
			}

			authority.author(username);

			return true;

		}
		return false;
	}

	public boolean isLogined(IOSession session) {
		return context.isLogined(session);
	}

	public void logout(IOSession session) {
		ActiveAuthority authority = (ActiveAuthority) session.getAuthority();
		if (authority != null) {
			authority.unauthor();
		}
	}

	public boolean isValidate(IOSession session, ServerReadFuture future) {

		Parameters param = future.getParameters();
		String username = param.getParameter("username");
		String password = param.getParameter("password");

		return this.username.equals(username) && this.password.equals(password);
	}

	public void initialize(ServerContext context, Configuration config) throws Exception {
		this.username = config.getProperty("USERNAME", "admin");
		this.password = config.getProperty("PASSWORD", "admin100");
	}

}
