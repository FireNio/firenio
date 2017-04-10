/*
 * Copyright 2015-2017 GenerallyCloud.com
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package com.generallycloud.baseio.container.authority;

import java.util.HashMap;
import java.util.Map;

import com.generallycloud.baseio.common.Encoding;
import com.generallycloud.baseio.common.FileUtil;
import com.generallycloud.baseio.common.FixedProperties;
import com.generallycloud.baseio.common.Logger;
import com.generallycloud.baseio.common.LoggerFactory;
import com.generallycloud.baseio.common.MD5Token;
import com.generallycloud.baseio.component.Parameters;
import com.generallycloud.baseio.component.SocketSession;
import com.generallycloud.baseio.container.AbstractInitializeable;
import com.generallycloud.baseio.container.ApplicationContext;
import com.generallycloud.baseio.container.LoginCenter;
import com.generallycloud.baseio.container.configuration.Configuration;

public class AuthorityLoginCenter extends AbstractInitializeable implements LoginCenter {
	
	private Logger logger = LoggerFactory.getLogger(AuthorityLoginCenter.class);

	private Map<String, Authority>	authorities	= new HashMap<String, Authority>();

	@Override
	public boolean login(SocketSession session, Parameters parameters) {

		String username = parameters.getParameter("username");
		String password = parameters.getParameter("password");
		
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
	public boolean isValidate(Parameters parameters) {

		String username = parameters.getParameter("username");
		String password = parameters.getParameter("password");

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
		
		String spPath = context.getRootLocalAddres() + "conf/server.properties";
		
		FixedProperties fixedProperties = FileUtil.readPropertiesByFile(spPath, Encoding.UTF8);
		
		String username = fixedProperties.getProperty("SERVER.USERNAME", "admin");
		String password = fixedProperties.getProperty("SERVER.PASSWORD", "admin100");
		String UUID = fixedProperties.getProperty("SERVER.UUID","uuid");
		Integer roleID = fixedProperties.getIntegerProperty("SERVER.ROLEID");

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
