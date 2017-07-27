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
package com.generallycloud.baseio.component;

import com.generallycloud.baseio.component.AbstractSocketSessionManager.SocketSessionManagerEvent;

/**
 * @author wangkai
 *
 */
public class AioGlobalSocketSessionManager implements SocketSessionManager {
	
	private SocketSessionManager sessionManager;
	
	public AioGlobalSocketSessionManager(SocketSessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	@Override
	public int getManagedSessionSize(){
		return sessionManager.getManagedSessionSize();
	}

	@Override
	public SocketSession getSession(int sessionId){
		return sessionManager.getSession(sessionId);
	}

	@Override
	public void offerSessionMEvent(SocketSessionManagerEvent event){
		sessionManager.offerSessionMEvent(event);
	}

	@Override
	public void loop() {
		
	}

	@Override
	public void stop() {
		
	}

	@Override
	public void putSession(SocketSession session) {
	}

	@Override
	public void removeSession(SocketSession session) {
	}

}
