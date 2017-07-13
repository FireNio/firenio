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

	private List<T>	machines;

	private T[]		nodes;

	private int		maxNode;

	@SuppressWarnings("unchecked")
	public NodeGroup(int nodes) {
		this.machines = new ArrayList<>(nodes / 2);
		this.nodes = (T[]) new Object[nodes];
		this.maxNode = nodes;
	}

	public synchronized void addMachine(T machine) {

		List<T> machines = this.machines;
		T[] nodes = this.nodes;
		int nsize = maxNode;

		if (machines.size() == 0) {
			machines.add(machine);
			for (int i = 0; i < nsize; i++) {
				nodes[i] = machine;
			}
			return;
		}

		int msize = machines.size();
		int nmsize = msize + 1;
		int replaceIndex = 0;
		int remain = nmsize;

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

			T n = nodes[i];
			if (n == replace) {
				nodes[i] = machine;
				replaced = true;
				i += remain;
				continue;
			}
			remain--;
			i++;
		}

		machines.add(machine);
	}

	public synchronized void removeMachine(T machine) {

		List<T> machines = this.machines;
		T[] nodes = this.nodes;
		int nsize = maxNode;

		machines.remove(machine);

		if (machines.size() == 0) {
			for (int i = 0; i < nsize; i++) {
				nodes[i] = null;
			}
			return;
		}

		int msize = machines.size();
		int replaceIndex = 0;
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
			T n = nodes[i];
			if (n == machine) {
				nodes[i] = replace;
				replaced = true;
			}
		}
	}

	private void count() {
		Map<T, AtomicInteger> map = new HashMap<>();
		if (machines.isEmpty()) {
			return;
		}
		for (T machine : machines) {
			map.put(machine, new AtomicInteger());
		}

		for (T node : nodes) {
			AtomicInteger a = map.get(node);
			if (a == null) {
				continue;
			}
			a.incrementAndGet();
		}

		System.out.println(map);
	}

	public T getMachine(int hash) {
		if (hash < maxNode) {
			return nodes[hash];
		}
		return nodes[hash % maxNode];
	}

	public static void main(String[] args) {

		NodeGroup<String> group = new NodeGroup<>(1024);

		int time = 8;

		List<String> machines = new ArrayList<>(time);

		for (int i = 0; i < time; i++) {
			String m = String.valueOf(i);
			machines.add(m);
			group.addMachine(m);
			group.count();
		}

		//		System.out.println();
		//		System.out.println("============================================================");
		//		System.out.println();

		for (int i = 0; i < time; i++) {
			group.removeMachine(machines.get(i));
			group.count();
		}

		System.out.println();
	}

}
