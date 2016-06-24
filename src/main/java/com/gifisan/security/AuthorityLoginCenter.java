package com.gifisan.security;

import java.util.HashMap;
import java.util.Map;

import com.gifisan.nio.common.Logger;
import com.gifisan.nio.common.LoggerFactory;
import com.gifisan.nio.common.MD5Token;
import com.gifisan.nio.common.SharedBundle;
import com.gifisan.nio.common.UUIDGenerator;
import com.gifisan.nio.component.ApplicationContext;
import com.gifisan.nio.component.Configuration;
import com.gifisan.nio.component.InitializeableImpl;
import com.gifisan.nio.component.LoginCenter;
import com.gifisan.nio.component.Parameters;
import com.gifisan.nio.component.Session;
import com.gifisan.nio.component.future.ReadFuture;
import com.gifisan.nio.plugin.authority.AuthoritySessionAttachment;
import com.gifisan.nio.plugin.authority.AuthorityContext;

public class AuthorityLoginCenter extends InitializeableImpl implements LoginCenter {
	
	private Logger logger = LoggerFactory.getLogger(AuthorityLoginCenter.class);

	private Map<String, Authority>	authorities	= new HashMap<String, Authority>();

	public boolean login(Session session, ReadFuture future) {

		Authority authority = getAuthority(session, future);
		
		logger.debug("__________________user_login__{}",authority);
		
		if (authority == null) {
			return false;
		}
		
		Parameters parameters = future.getParameters();
		
		String machineType = parameters.getParameter("MATCH_TYPE");
		
		AuthorityContext authorityPlugin = AuthorityContext.getInstance();
		
		AuthoritySessionAttachment attachment = (AuthoritySessionAttachment) session.getAttachment(authorityPlugin);
		
		ApplicationContext context = ApplicationContext.getInstance();
		
		RoleManager roleManager = context.getRoleManager();
		
		AuthorityManager authorityManager = roleManager.getAuthorityManager(authority);
		
		attachment.setAuthorityManager(authorityManager);
		
//		session.setSessionID(UUIDGenerator.random()); FIXME ...
		context.getSessionFactory().putSession(session);
		
		session.setMachineType(machineType);
		
		return true;

	}

	public boolean isLogined(Session session) {
		AuthorityContext authorityPlugin = AuthorityContext.getInstance();
		
		AuthoritySessionAttachment attachment = (AuthoritySessionAttachment) session.getAttachment(authorityPlugin);
		
		return attachment.getAuthorityManager() != null;
	}

	public void logout(Session session) {
		
		// 需要登出吗
	}

	public boolean isValidate(Session session, ReadFuture future) {

		return getAuthority(session, future) != null;
	}
	
	protected Authority getAuthority(Session session, ReadFuture future) {

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
	

	public void initialize(ApplicationContext context, Configuration config) throws Exception {
		
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
		authority.setPassword(MD5Token.getInstance().getLongToken("udp1", context.getEncoding()));
		authority.setRoleID(0);
		authority.setUuid("udp1");
		
		this.authorities.put(authority.getUsername(), authority);
		
		authority = new Authority();

		authority.setUsername("udp2");
		authority.setPassword(MD5Token.getInstance().getLongToken("udp2", context.getEncoding()));
		authority.setRoleID(0);
		authority.setUuid("udp2");
		
		this.authorities.put(authority.getUsername(), authority);
		
	}

}
