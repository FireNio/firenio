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

	public void addRouterSession(BalanceReverseSocketSession session) {
		nodeGroup.addMachine(session);
	}

	public void removeRouterSession(BalanceReverseSocketSession session) {
		nodeGroup.removeMachine(session);
	}

	public BalanceReverseSocketSession getRouterSession(BalanceFacadeSocketSession session, ReadFuture future) {

		HashedBalanceReadFuture f = (HashedBalanceReadFuture) future;

		return nodeGroup.getMachine(f.getHashCode());
	}

	public BalanceReverseSocketSession getRouterSession(BalanceFacadeSocketSession session) {
		return null;
	}

}
