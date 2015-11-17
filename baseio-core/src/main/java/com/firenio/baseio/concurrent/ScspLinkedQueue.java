/*
 * Copyright 2015 The Baseio Project
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
package com.firenio.baseio.concurrent;

import java.util.concurrent.atomic.AtomicInteger;

public class ScspLinkedQueue<V> {

    private volatile Node<V> head = null;                           // volatile ?
    //     not sure if this useful
    long                     p00, p01, p02, p03, p04, p05, p06, p07;
    long                     p10, p11, p12, p13, p14, p15, p16, p17;

    private AtomicInteger    size = new AtomicInteger();

    private volatile Node<V> tail = null;

    public ScspLinkedQueue() {
        this.head = new Node<>();
        this.tail = head;
    }

    Node<V> getTail() {
        return tail;
    }

    protected int incrementAndGet() {
        return size.incrementAndGet();
    }

    public void offer(V v) {
        Node<V> node = new Node<>();
        tail.v = v;
        tail.next = node;
        tail = node;
        size.incrementAndGet();
    }

    public V poll() {
        if (size.get() == 0) {
            return null;
        }
        size.decrementAndGet();
        Node<V> h = head;
        head = h.next;
        return h.v;
    }

    public int size() {
        return size.get();
    }

    static class Node<V> {
        Node<V> next;
        V       v;
    }

}
