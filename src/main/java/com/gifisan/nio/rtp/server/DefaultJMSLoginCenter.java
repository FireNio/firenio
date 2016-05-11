package com.gifisan.nio.rtp.server;

import com.gifisan.nio.AbstractLifeCycle;
import com.gifisan.nio.common.SharedBundle;
import com.gifisan.nio.component.ActiveAuthority;
import com.gifisan.nio.component.Authority;
import com.gifisan.nio.component.DefaultAuthority;
import com.gifisan.nio.component.LoginCenter;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.IOSession;
import com.gifisan.nio.server.ServerSession;

public class DefaultJMSLoginCenter extends AbstractLifeCycle implements LoginCenter {

	private String		username	= null;
	private String		password	= null;

	public Authority login(IOSession session, ServerReadFuture future) {
		
		ServerSession _Session = (ServerSession) session;
		
		if (validate(session, future)) {
			
			ActiveAuthority authority = _Session.getAuthority();

			if (authority == null) {
				authority = new DefaultAuthority();
				
				RTPSessionAttachment attachment = (RTPSessionAttachment) _Session.attachment();
				
				attachment.setAuthority(authority);
			}

			authority.author(username);

			return authority;

		}
		return null;
	}

	public boolean logined(IOSession session, ServerReadFuture future) {
		
		RTPSessionAttachment attachment = (RTPSessionAttachment) session.attachment();
		
		return attachment.getAuthority() != null && attachment.getAuthority().isAuthored();
	}

	public void logout(IOSession session, ServerReadFuture future) {
		ActiveAuthority authority = (ActiveAuthority) session.getAuthority();
		if (authority != null) {
			authority.unauthor();
		}
	}

	public boolean validate(IOSession session, ServerReadFuture future) {
		
		Parameters param = future.getParameters();
		String username = param.getParameter("username");
		String password = param.getParameter("password");

		return this.username.equals(username) && this.password.equals(password);
	}

	protected void doStart() throws Exception {
		this.username = SharedBundle.instance().getProperty("SERVER.JMS.USERNAME", "admin");
		this.password = SharedBundle.instance().getProperty("SERVER.JMS.PASSWORD", "admin10000");
	}

	protected void doStop() throws Exception {

	}

}
