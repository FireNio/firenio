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
import java.util.List;

/**
 * @author wangkai
 *
 */
public class VirtualNodes<T extends VirtualMachine> {

    protected List<T> machines;

    protected T[]     nodes;

    private int       maxNode;

    private int       addBenchIndex;

    private int       removeBenchIndex;

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
        if (machines.size() == maxNode) {
            throw new RuntimeException("max nodes " + maxNode);
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

    public T getMachine(int hash) {
        return nodes[hash % maxNode];
    }

}
