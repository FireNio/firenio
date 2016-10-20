package com.generallycloud.nio.balance.router;

import com.generallycloud.nio.balance.HashedBalanceReadFuture;
import com.generallycloud.nio.component.IOSession;
import com.generallycloud.nio.protocol.ReadFuture;

public class HashedFrontRouter implements FrontRouter {

	public HashedFrontRouter(int maxNode) {
		this.nodeGroup = new NodeGroup(maxNode);
	}

	private NodeGroup	nodeGroup;

	public void addRouterSession(IOSession session) {
		Machine machine = new Machine(session);
		nodeGroup.addMachine(machine);
	}

	public void removeRouterSession(IOSession session) {
		Machine machine = (Machine) session.getAttachment();
		if (machine == null) {
			return;
		}
		nodeGroup.removeMachine(machine);
	}

	public IOSession getRouterSession(IOSession session, ReadFuture future) {

		HashedBalanceReadFuture f = (HashedBalanceReadFuture) future;

		return nodeGroup.getMachine(f.getHashCode()).session;
	}

	public IOSession getRouterSession(IOSession session) {

		Machine machine = (Machine) session.getAttachment();

		if (machine == null) {
			return null;
		}

		return machine.session;
	}
}
