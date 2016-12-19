package com.generallycloud.nio.container.authority;

import java.util.HashMap;
import java.util.Map;

import com.generallycloud.nio.common.Logger;
import com.generallycloud.nio.common.LoggerFactory;
import com.generallycloud.nio.common.MD5Token;
import com.generallycloud.nio.common.SharedBundle;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.ApplicationContext;
import com.generallycloud.nio.container.InitializeableImpl;
import com.generallycloud.nio.container.LoginCenter;
import com.generallycloud.nio.container.configuration.Configuration;

public class AuthorityLoginCenter extends InitializeableImpl implements LoginCenter {
	
	private Logger logger = LoggerFactory.getLogger(AuthorityLoginCenter.class);

	private Map<String, Authority>	authorities	= new HashMap<String, Authority>();

	@Override
	public boolean login(SocketSession session, String username,String password) {

		Authority authority = getAuthority(username,password);
		
		logger.debug("__________________user_login__{}",authority);
		
		if (authority == null) {
			return false;
		}
		
		AuthorityContext authorityPlugin = AuthorityContext.getInstance();
		
		AuthoritySessionAttachment attachment = (AuthoritySessionAttachment) session.getAttachment(authorityPlugin.getPluginIndex());
		
		ApplicationContext context = ApplicationContext.getInstance();
		
		RoleManager roleManager = context.getRoleManager();
		
		AuthorityManager authorityManager = roleManager.getAuthorityManager(authority);
		
		attachment.setAuthorityManager(authorityManager);
		
		return true;

	}

	@Override
	public boolean isLogined(SocketSession session) {
		
		AuthorityContext authorityPlugin = AuthorityContext.getInstance();
		
		AuthoritySessionAttachment attachment = (AuthoritySessionAttachment) session.getAttachment(authorityPlugin.getPluginIndex());
		
		return attachment.getAuthorityManager() != null;
	}

	@Override
	public void logout(SocketSession session) {
		
		// 需要登出吗
	}

	@Override
	public boolean isValidate(String username,String password) {

		return getAuthority(username,password) != null;
	}
	
	protected Authority getAuthority(String username,String password) {

		Authority authority = authorities.get(username);
		
		if (authority == null) {
			return null;
		}
		
		if(!authority.getPassword().equals(password)){
			return null;
		}
		
		return authority;
	}
	

	@Override
	public void initialize(ApplicationContext context, Configuration config) throws Exception {
		
		String username = SharedBundle.instance().getProperty("SERVER.USERNAME", "admin");
		String password = SharedBundle.instance().getProperty("SERVER.PASSWORD", "admin100");
		String UUID = SharedBundle.instance().getProperty("SERVER.UUID","uuid");
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
