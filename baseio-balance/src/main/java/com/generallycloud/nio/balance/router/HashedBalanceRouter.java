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
import com.generallycloud.nio.balance.HashedBalanceReadFuture;
import com.generallycloud.nio.protocol.ReadFuture;

public class HashedBalanceRouter extends AbstractBalanceRouter {

	public HashedBalanceRouter(int maxNode) {
		this.nodeGroup = new NodeGroup(maxNode);
	}

	private NodeGroup	nodeGroup;

	@Override
	public void addRouterSession(BalanceReverseSocketSession session) {
		nodeGroup.addMachine(session);
	}

	@Override
	public void removeRouterSession(BalanceReverseSocketSession session) {
		nodeGroup.removeMachine(session);
	}

	@Override
	public BalanceReverseSocketSession getRouterSession(BalanceFacadeSocketSession session, ReadFuture future) {

		HashedBalanceReadFuture f = (HashedBalanceReadFuture) future;

		return nodeGroup.getMachine(f.getHashCode());
	}

	@Override
	public BalanceReverseSocketSession getRouterSession(BalanceFacadeSocketSession session) {
		return null;
	}

}
