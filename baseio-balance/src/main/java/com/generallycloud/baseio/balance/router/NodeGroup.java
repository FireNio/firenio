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
package com.generallycloud.baseio.balance.router;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wangkai
 *
 */
public class NodeGroup<T> {

	private List<T>		machines;

	//FIXME 这里直接使用T
	private List<Node<T>>	nodes;

	private int			maxNode;

	public NodeGroup(int nodes) {
		this.machines = new ArrayList<>(nodes / 2);
		this.nodes = initNodes(nodes);
		this.maxNode = nodes;
	}

	private List<Node<T>> initNodes(int nodes) {
		List<Node<T>> ns = new ArrayList<>(nodes);
		for (int i = 0; i < nodes; i++) {
			ns.add(new Node<T>());
		}
		return ns;
	}

	public synchronized void addMachine(T machine) {

		List<T> machines = this.machines;
		List<Node<T>> nodes = this.nodes;

		if (machines.size() == 0) {
			machines.add(machine);
			for (int i = 0; i < nodes.size(); i++) {
				nodes.get(i).setMachine(machine);
			}
			return;
		}

		int msize = machines.size();
		int nmsize = msize + 1;
		int replaceIndex = 0;
		int remain = nmsize;
		int nsize = nodes.size();
		boolean replaced = true;
		T replace = null;
		for (int i = msize; i < nsize;) {

			if (replaced) {
				replaced = false;
				remain = nmsize;
				if (replaceIndex == msize) {
					replaceIndex = 0;
				}
				replace = machines.get(replaceIndex++);
			}

			Node<T> n = nodes.get(i++);
			remain--;
			if (n.getMachine() == replace) {
				n.setMachine(machine);
				replaced = true;
				i += remain;
			}

		}

		machines.add(machine);
	}

	public synchronized void removeMachine(T machine) {

		List<T> machines = this.machines;
		List<Node<T>> nodes = this.nodes;

		machines.remove(machine);

		if (machines.size() == 0) {
			for (int i = 0; i < nodes.size(); i++) {
				nodes.get(i).setMachine(null);
			}
			return;
		}

		int msize = machines.size();
		int replaceIndex = 0;
		int nsize = nodes.size();
		boolean replaced = true;
		T replace = null;
		for (int i = 0; i < nsize; i++) {
			if (replaced) {
				replaced = false;
				if (replaceIndex == msize) {
					replaceIndex = 0;
				}
				replace = machines.get(replaceIndex++);
			}
			Node<T> n = nodes.get(i);
			if (n.getMachine() == machine) {
				n.setMachine(replace);
				replaced = true;
			}
		}
	}

	private void count() {
		Map<T, AtomicInteger> map = new HashMap<>();
		Map<T, AtomicInteger> map1 = new HashMap<>();
		if (machines.isEmpty()) {
			return;
		}
		for (T machine : machines) {
			map.put(machine, new AtomicInteger());
			map1.put(machine, new AtomicInteger());
		}

		for (Node<T> node : nodes) {
			map.get(node.getMachine()).incrementAndGet();
			if (node.compare()) {
				map1.get(node.getMachine()).incrementAndGet();
			}
		}

		System.out.println(map);
		System.out.println(map1);

	}

	public T getMachine(int hash) {
		if (hash < maxNode) {
			return nodes.get(hash).getMachine();
		}
		return nodes.get(hash % maxNode).getMachine();
	}

	private void setLast() {
		for (Node<T> node : nodes) {
			node.setLast(node.getMachine());
		}
	}

	public static void main(String[] args) {

		NodeGroup<String> group = new NodeGroup<>(1024);

		int time = 8;

		List<String> machines = new ArrayList<>(time);

		for (int i = 0; i < time; i++) {
			String m = String.valueOf(i);
			machines.add(m);
			group.setLast();
			group.addMachine(m);
			group.count();
		}

		//		System.out.println();
		//		System.out.println("============================================================");
		//		System.out.println();

		for (int i = 0; i < time; i++) {
			group.setLast();
			group.removeMachine(machines.get(i));
			group.count();
		}

		System.out.println();
	}

}
