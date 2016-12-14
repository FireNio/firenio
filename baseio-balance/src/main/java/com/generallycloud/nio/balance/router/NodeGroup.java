package com.generallycloud.nio.balance.router;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.generallycloud.nio.balance.BalanceReverseSocketSession;

public class NodeGroup {

	private ReentrantLock					lock		= new ReentrantLock();

	private int							maxNode;

	private Node[]							nodes;

	private List<BalanceReverseSocketSession>	machines	= new ArrayList<BalanceReverseSocketSession>();

	public NodeGroup(int maxNode) {
		this.maxNode = maxNode;
		this.initialize();
	}

	private void initialize() {
		this.nodes = new Node[maxNode];
		for (int i = 0; i < nodes.length; i++) {
			nodes[i] = new Node(i);
		}
	}

	public void addMachine(BalanceReverseSocketSession machine) {

		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			machines.add(machine);

			machineChange();

		} finally {

			lock.unlock();
		}
	}

	public void removeMachine(BalanceReverseSocketSession machine) {

		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			if(!machines.remove(machine)){
				return;
			}
			
			machineChange();

		} finally {

			lock.unlock();
		}
	}

	private void machineChange() {
		
		if (machines.isEmpty()) {
			return;
		}

		List<BalanceReverseSocketSession> machines = this.machines;

		Node[] nodes = this.nodes;

		int m_index = 0;

		int m_size = machines.size();

		for (Node n : nodes) {

			if (m_index == m_size) {
				m_index = 0;
			}

			n.machine = machines.get(m_index++);
		}
	}

	// FIXME 是否要设置同步
	public BalanceReverseSocketSession getMachine(int hash) {
		if (hash < maxNode) {
			return nodes[hash].machine;
		}
		return nodes[hash % maxNode].machine;
	}

}
