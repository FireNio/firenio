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
package com.generallycloud.baseio.front;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class FrontRouter {

	private ConcurrentMap<Long, FrontFacadeSocketSession> clients = new ConcurrentHashMap<>();

	public void addClientSession(FrontFacadeSocketSession session) {
//		this.clients.put(session.get, session);
	}

	public FrontFacadeSocketSession getClientSession(Long sessionID) {
		return clients.get(sessionID);
	}

	public void removeClientSession(FrontFacadeSocketSession session) {
//		this.clients.remove(session.getSessionID());
	}
	
}
