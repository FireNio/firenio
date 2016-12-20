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
