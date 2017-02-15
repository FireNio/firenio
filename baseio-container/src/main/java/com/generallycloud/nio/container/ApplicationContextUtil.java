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
package com.generallycloud.nio.container;

import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.container.authority.Authority;
import com.generallycloud.nio.container.authority.AuthorityContext;
import com.generallycloud.nio.container.authority.AuthorityManager;
import com.generallycloud.nio.container.authority.AuthoritySessionAttachment;

public class ApplicationContextUtil {

	public static AuthorityManager getAuthorityManager(SocketSession session){
		
		AuthorityContext plugin = AuthorityContext.getInstance();
		
		AuthoritySessionAttachment attachment = (AuthoritySessionAttachment) session.getAttachment(plugin.getPluginIndex());
		
		if (attachment == null) {
			return null;
		}
		
		return attachment.getAuthorityManager();
	}
	
	public static Authority getAuthority(SocketSession session){
		
		AuthorityManager authorityManager = getAuthorityManager(session);
		
		if (authorityManager == null) {
			return null;
		}
		
		return authorityManager.getAuthority();
	}
	
}
