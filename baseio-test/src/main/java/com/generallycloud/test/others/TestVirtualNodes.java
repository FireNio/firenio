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
package com.generallycloud.test.others;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import com.generallycloud.baseio.balance.router.VirtualMachine;
import com.generallycloud.baseio.balance.router.VirtualNodes;

/**
 * @author wangkai
 *
 */
public class TestVirtualNodes extends VirtualNodes<VirtualMachine> {

    private VirtualMachine[] lastNodes;

    public TestVirtualNodes(int nodes) {
        super(nodes);
    }

    public static void main(String[] args) {
        test3();
    }

    static void test2() {

        int node = new Random().nextInt(1000000) + 100;

        TestVirtualNodes group = new TestVirtualNodes(node);

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

    static void test3() {

        int node = 9999;

        int time = 10;

        TestVirtualNodes group = new TestVirtualNodes(node);

        List<StringMachine> machines = new ArrayList<>(time);

        for (int i = 0; i < time; i++) {
            group.lastNodes = group.nodes.clone();
            StringMachine m = new StringMachine(String.valueOf(i));
            machines.add(m);
            group.addMachine(m);
            group.count();
        }

        for (int i = 0; i < time; i++) {
            group.lastNodes = group.nodes.clone();
            int index = new Random().nextInt(machines.size());
            group.removeMachine(machines.remove(index));
            group.count();
        }

        System.out.println();
    }

    static void test1() {

        int node = new Random().nextInt(1000000) + 100;

        TestVirtualNodes group = new TestVirtualNodes(node);

        int time = new Random().nextInt(20) + 1;

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

    private void count() {
        int max = -1;
        int min = Integer.MAX_VALUE;
        Map<VirtualMachine, AtomicInteger> map = new LinkedHashMap<>();
        if (machines.isEmpty()) {
            return;
        }
        for (VirtualMachine machine : machines) {
            map.put(machine, new AtomicInteger());
        }

        for (VirtualMachine node : nodes) {
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

        int notEqual = 0;
        for (int j = 0; j < lastNodes.length; j++) {
            if (lastNodes[j] != nodes[j]) {
                notEqual++;
            }
        }

        System.out.print(map);
        //		ThreadUtil.sleep(1);
        String str = (max - min) + "====" + (notEqual * 1f / lastNodes.length);
        System.err.println(str);
    }

    static class StringMachine implements VirtualMachine {

        private String value;

        public StringMachine(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

}
