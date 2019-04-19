/*
 * Copyright 2015 The FireNio Project
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
package com.firenio.collection;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author wangkai
 */
public class LinkedBQStack<V> implements Stack<V> {

    private int                    max;
    private LinkedBlockingQueue<V> queue;

    public LinkedBQStack(int max) {
        this.max = max;
        this.queue = new LinkedBlockingQueue<>((max << 1) + max);
    }

    @Override
    public void clear() {
        queue.clear();
    }

    @Override
    public V pop() {
        if (queue.isEmpty()) {
            return null;
        }
        return queue.poll();
    }

    @Override
    public void push(V v) {
        if (queue.size() < max) {
            queue.offer(v);
        }
    }

    @Override
    public int size() {
        return queue.size();
    }

}
