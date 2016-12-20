/*
 * Copyright 2015 GenerallyCloud.com
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
import com.generallycloud.nio.protocol.ReadFuture;

public interface BalanceRouter {

	public abstract void addClientSession(BalanceFacadeSocketSession session);

	public abstract void addRouterSession(BalanceReverseSocketSession session);
	
	public abstract BalanceFacadeSocketSession getClientSession(Integer sessionID);

	public abstract BalanceReverseSocketSession getRouterSession(BalanceFacadeSocketSession session);
	
	public abstract BalanceReverseSocketSession getRouterSession(BalanceFacadeSocketSession session, ReadFuture future);

	public abstract void removeClientSession(BalanceFacadeSocketSession session);

	public abstract void removeRouterSession(BalanceReverseSocketSession session);

}