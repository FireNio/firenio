package com.generallycloud.nio.balance.router;

import com.generallycloud.nio.balance.HashedBalanceReadFuture;
import com.generallycloud.nio.component.SocketSession;
import com.generallycloud.nio.protocol.ReadFuture;

public class HashedFrontRouter extends AbstractFrontRouter {

	public HashedFrontRouter(int maxNode) {
		this.nodeGroup = new NodeGroup(maxNode);
	}

	private NodeGroup	nodeGroup;

	public void addRouterSession(SocketSession session) {
		Machine machine = new Machine(session);
		nodeGroup.addMachine(machine);
	}

	public void removeRouterSession(SocketSession session) {
		Machine machine = (Machine) session.getAttachment();
		if (machine == null) {
			return;
		}
		nodeGroup.removeMachine(machine);
	}

	public SocketSession getRouterSession(SocketSession session, ReadFuture future) {

		HashedBalanceReadFuture f = (HashedBalanceReadFuture) future;

		return nodeGroup.getMachine(f.getHashCode()).session;
	}

	public SocketSession getRouterSession(SocketSession session) {

		Machine machine = (Machine) session.getAttachment();

		if (machine == null) {
			return null;
		}

		return machine.session;
	}
}
