package com.generallycloud.nio.balance.router;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class NodeGroup {

	private ReentrantLock	lock		= new ReentrantLock();

	private int			maxNode;

	private Node[]			nodes;

	private List<Machine>	machines	= new ArrayList<Machine>();

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

	public void addMachine(Machine machine) {

		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			machines.add(machine);

			machineChange();

		} finally {

			lock.unlock();
		}
	}

	public void removeMachine(Machine machine) {

		ReentrantLock lock = this.lock;

		lock.lock();

		try {

			machines.remove(machine);

			machineChange();

		} finally {

			lock.unlock();
		}
	}

	private void machineChange() {

		List<Machine> machines = this.machines;

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

	//FIXME 是否要设置同步
	public Machine getMachine(int hash) {
		if (hash < maxNode) {
			return nodes[hash].machine;
		}
		return nodes[hash % maxNode].machine;
	}

	public static void main(String[] args) {

	}
}
