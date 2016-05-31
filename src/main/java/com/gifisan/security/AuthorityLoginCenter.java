package com.gifisan.security;

import java.util.HashMap;
import java.util.Map;

import com.gifisan.nio.common.SharedBundle;
import com.gifisan.nio.common.UUIDGenerator;
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
		
		Parameters parameters = future.getParameters();
		
		String machineType = parameters.getParameter("MATCH_TYPE");
		
		ServerContext context = session.getContext();
		
		RoleManager roleManager = context.getRoleManager();
		
		AuthorityManager authorityManager = roleManager.getAuthorityManager(authority);
		
		setAuthorityInfo((ServerSession)session, authorityManager,machineType);
		
		authority.setSessionID(session.getSessionID());

		return true;

	}

	public boolean isLogined(IOSession session) {
		return ((ServerSession)session).getAuthorityManager() != null;
	}

	public void logout(IOSession session) {
		
		// 需要登出吗
	}
	
	private void setAuthorityInfo(ServerSession session,AuthorityManager authorityManager,String machineType){
		
		session.setAuthorityManager(authorityManager);
		session.setMachineType(machineType);
	}

	public boolean isValidate(IOSession session, ServerReadFuture future) {

		return getAuthority(session, future) != null;
	}
	
	protected Authority getAuthority(IOSession session, ServerReadFuture future) {

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
		String UUID = SharedBundle.instance().getProperty("SERVER.UUID",UUIDGenerator.random());
		Integer roleID = SharedBundle.instance().getIntegerProperty("SERVER.ROLEID");
		

		Authority authority = new Authority();

		authority.setUsername(username);
		authority.setPassword(password);
		authority.setRoleID(roleID);
		authority.setUuid(UUID);
		
		/*     -------------------------------------------------------------   */
		
		this.authorities.put(authority.getUsername(), authority);
		
		authority = new Authority();

		authority.setUsername("udp1");
		authority.setPassword("udp1");
		authority.setRoleID(0);
		authority.setUuid("udp1");
		
		this.authorities.put(authority.getUsername(), authority);
		
		authority = new Authority();

		authority.setUsername("udp2");
		authority.setPassword("udp2");
		authority.setRoleID(0);
		authority.setUuid("udp2");
		
		this.authorities.put(authority.getUsername(), authority);
		
	}

}
