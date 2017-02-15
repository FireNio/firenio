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
package com.generallycloud.nio.balance.router;

import com.generallycloud.nio.balance.BalanceFacadeSocketSession;
import com.generallycloud.nio.balance.BalanceReverseSocketSession;
import com.generallycloud.nio.component.concurrent.ReentrantMap;

public abstract class AbstractBalanceRouter implements BalanceRouter{

	private ReentrantMap<Integer, BalanceFacadeSocketSession> clients = new ReentrantMap<>();

	@Override
	public void addClientSession(BalanceFacadeSocketSession session) {
		this.clients.put(session.getSessionID(), session);
	}

	@Override
	public BalanceFacadeSocketSession getClientSession(Integer sessionID) {
		return clients.get(sessionID);
	}

	@Override
	public void removeClientSession(BalanceFacadeSocketSession session) {
		this.clients.remove(session.getSessionID());
	}
	
	@Override
	public BalanceReverseSocketSession getRouterSession(BalanceFacadeSocketSession session) {
		return session.getReverseSocketSession();
	}
}
