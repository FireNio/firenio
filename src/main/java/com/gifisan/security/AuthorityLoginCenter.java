package com.gifisan.security;

import java.util.HashMap;
import java.util.Map;

import com.gifisan.nio.common.SharedBundle;
import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.component.InitializeableImpl;
import com.gifisan.nio.component.LoginCenter;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.future.ServerReadFuture;
import com.gifisan.nio.server.IOSession;
import com.gifisan.nio.server.ServerContext;
import com.gifisan.nio.server.ServerSession;

public class AuthorityLoginCenter extends InitializeableImpl implements LoginCenter {

	private Map<String, Authority>	authorities	= new HashMap<String, Authority>();

	public boolean login(IOSession session, ServerReadFuture future) {

		Authority authority = getAuthority(session, future);
		
		if (authority == null) {
			return false;
		}

		ServerContext context = session.getContext();
		
		RoleManager roleManager = context.getRoleManager();
		
		AuthorityManager authorityManager = roleManager.getAuthorityManager(authority.getRoleID());
		
		setAuthorityManager(session, authorityManager);

		return true;

	}

	public boolean isLogined(IOSession session) {
		return ((ServerSession)session).getAuthorityManager() != null;
	}

	public void logout(IOSession session) {
		
		setAuthorityManager(session, null);
	}
	
	private void setAuthorityManager(IOSession session,AuthorityManager authorityManager){
		
		((ServerSession)session).setAuthorityManager(null);
	}

	public boolean isValidate(IOSession session, ServerReadFuture future) {

		return getAuthority(session, future) != null;
	}
	
	private Authority getAuthority(IOSession session, ServerReadFuture future) {

		Parameters param = future.getParameters();
		String username = param.getParameter("username");
		String password = param.getParameter("password");
		
		Authority authority = authorities.get(username);
		
		if (authority == null) {
			return null;
		}
		
		if(!authority.getPassword().equals(password)){
			return null;
		}
		return authority;
	}
	

	public void initialize(ServerContext context, Configuration config) throws Exception {
		String username = SharedBundle.instance().getProperty("SERVER.USERNAME", "admin");
		String password = SharedBundle.instance().getProperty("SERVER.PASSWORD", "admin10000");
		Integer roleID = SharedBundle.instance().getIntegerProperty("roleID");

		Authority authority = new Authority();

		authority.setUsername(username);
		authority.setPassword(password);
		authority.setRoleID(roleID);
		
		this.authorities.put(authority.getUsername(), authority);
		
	}

}
