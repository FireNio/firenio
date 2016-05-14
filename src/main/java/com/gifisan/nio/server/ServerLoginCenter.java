package com.gifisan.nio.server;

import com.gifisan.nio.common.SharedBundle;
import com.gifisan.nio.component.ActiveAuthority;
import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.component.DefaultAuthority;
import com.gifisan.nio.component.InitializeableImpl;
import com.gifisan.nio.component.LoginCenter;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.future.ServerReadFuture;

public class ServerLoginCenter extends InitializeableImpl implements LoginCenter {

	private String			username	= null;
	private String			password	= null;

	public boolean login(IOSession session, ServerReadFuture future) {

		if (isValidate(session, future)) {

			ServerSession _session = (ServerSession) session;

			ActiveAuthority authority = _session.getAuthority();

			if (authority == null) {
				authority = new DefaultAuthority();
				_session.setAuthority(authority);
			}

			authority.author(username);

			return true;

		}
		return false;
	}

	public boolean isLogined(IOSession session) {
		return session.getAuthority() != null && session.getAuthority().isAuthored();
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
		this.username = SharedBundle.instance().getProperty("SERVER.USERNAME", "admin");
		this.password = SharedBundle.instance().getProperty("SERVER.PASSWORD", "admin10000");
	}

}
