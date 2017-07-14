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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import com.generallycloud.baseio.common.ThreadUtil;

/**
 * @author wangkai
 *
 */
public class VirtualNodes<T extends VirtualMachine> {

	private List<T>	machines;

	private T[]		nodes;

	private int		maxNode;

	private int		addBenchIndex;

	private int		removeBenchIndex;

	@SuppressWarnings("unchecked")
	public VirtualNodes(int nodes) {
		this.machines = new ArrayList<>(nodes / 2);
		this.nodes = (T[]) new VirtualMachine[nodes];
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
		int remain = nmsize;
		T replace = getNextAddBench(machines);
		for (int i = 0; i < nsize;) {
			if (nodes[i] == replace) {
				nodes[i] = machine;
				replace = getNextAddBench(machines);
				i += remain;
				remain = nmsize;
				continue;
			}
			i++;
			remain--;
		}
		goBackAddBench(machines);
		machines.add(machine);
	}

	private void goBackAddBench(List<T> machines) {
		if (addBenchIndex == 0) {
			addBenchIndex = machines.size() - 1;
		} else {
			addBenchIndex--;
		}
	}

	private void goBackRemoveBench(List<T> machines) {
		if (removeBenchIndex == 0) {
			removeBenchIndex = machines.size() - 1;
		} else {
			removeBenchIndex--;
		}
	}

	private void setAllMachine(T[] nodes, int nsize, T replace) {
		for (int i = 0; i < nsize; i++) {
			nodes[i] = replace;
		}
	}

	private void reIndexOfBench(List<T> machines, T machine) {
		int i = machines.indexOf(machine);
		if (i < addBenchIndex) {
			goBackAddBench(machines);
		}
		if (i < removeBenchIndex) {
			goBackRemoveBench(machines);
		}
	}

	public synchronized void removeMachine(T machine) {
		List<T> machines = this.machines;
		T[] nodes = this.nodes;
		int nsize = maxNode;
		reIndexOfBench(machines, machine);
		machines.remove(machine);
		if (machines.size() == 0) {
			setAllMachine(nodes, nsize, null);
			return;
		} else if (machines.size() == 1) {
			setAllMachine(nodes, nsize, machines.get(0));
			return;
		}
		for (int i = 0; i < nsize; i++) {
			if (nodes[i] == machine) {
				nodes[i] = getNextRemoveBench(machines);
				continue;
			}
		}
	}

	private T getNextAddBench(List<T> machines) {
		if (addBenchIndex >= machines.size()) {
			addBenchIndex = 1;
			return machines.get(0);
		}
		return machines.get(addBenchIndex++);
	}

	private T getNextRemoveBench(List<T> machines) {
		if (removeBenchIndex >= machines.size()) {
			removeBenchIndex = 1;
			return machines.get(0);
		}
		return machines.get(removeBenchIndex++);
	}

	private void count() {
		int max = -1;
		int min = Integer.MAX_VALUE;
		Map<T, AtomicInteger> map = new LinkedHashMap<>();
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

		for (AtomicInteger a : map.values()) {
			if (a.get() > max) {
				max = a.get();
			}
			if (a.get() < min) {
				min = a.get();
			}
		}

		System.out.print(map);
		ThreadUtil.sleep(1);
		System.err.println("\t" + new AtomicInteger(max - min));
	}

	public T getMachine(int hash) {
		return nodes[hash % maxNode];
	}

	public static void main(String[] args) {

		test2();

	}

	static void test2() {

		int node = new Random().nextInt(1000000) + 100;

		VirtualNodes<StringMachine> group = new VirtualNodes<>(node);

		int time = new Random().nextInt(50) + 1;
		System.out.println(time);

		List<StringMachine> machines = new ArrayList<>(time);

		int machineIndex = 0;

		for (int i = 0; i < 5; i++) {
			StringMachine m = new StringMachine(String.valueOf(machineIndex++));
			machines.add(m);
			group.addMachine(m);
			group.count();
		}

		for (int i = 0; i < time; i++) {
			int r = new Random().nextInt(100);
			if (r > 35) {
				StringMachine m = new StringMachine(String.valueOf(machineIndex++));
				machines.add(m);
				group.addMachine(m);
				group.count();
			} else {
				if (machines.size() < 5) {
					continue;
				}
				int index = new Random().nextInt(machines.size());
				group.removeMachine(machines.remove(index));
				group.count();
			}
		}

		System.out.println();

	}

	static void test1() {

		int node = new Random().nextInt(1000000) + 100;

		VirtualNodes<StringMachine> group = new VirtualNodes<>(node);

		int time = new Random().nextInt(10) + 1;

		List<StringMachine> machines = new ArrayList<>(time);

		for (int i = 0; i < time; i++) {
			StringMachine m = new StringMachine(String.valueOf(i));
			machines.add(m);
			group.addMachine(m);
			group.count();
		}

		for (int i = 0; i < time; i++) {
			int index = new Random().nextInt(machines.size());
			group.removeMachine(machines.remove(index));
			group.count();
		}

		System.out.println();

	}

	static class StringMachine implements VirtualMachine {

		private String		value;

		public StringMachine(String value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return value;
		}
	}
}
