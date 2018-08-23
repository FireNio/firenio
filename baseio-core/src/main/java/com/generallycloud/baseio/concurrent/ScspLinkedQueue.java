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
package com.generallycloud.baseio.concurrent;

import java.util.concurrent.atomic.AtomicInteger;

public class ScspLinkedQueue<V> {

    private AtomicInteger size = new AtomicInteger();
    private Node<V>       head = null;               // volatile ?
    private Node<V>       tail = null;               // volatile ?

    public ScspLinkedQueue() {
        this.head = new Node<>(null);
        this.tail = head;
        this.tail.validate = false;
    }

    public void offer(V v) {
        Node<V> node = new Node<>(v);
        tail.next = node;
        tail = node;
        size.incrementAndGet();
    }

    public V poll() {
        if (size.get() == 0) {
            return null;
        }
        return get(head);
    }

    private V get(Node<V> h) {
        if (h.validate) {
            Node<V> next = h.next;
            if (next == null) {
                h.validate = false;
                head = h;
            } else {
                head = next;
            }
            this.size.decrementAndGet();
            return h.v;
        } else {
            return get(h.next);
        }
    }
    
    public Node<V> getHead() {
        return head;
    }

    public void setHead(Node<V> head) {
        this.head = head;
    }

    public Node<V> getTail() {
        return tail;
    }

    public void setTail(Node<V> tail) {
        this.tail = tail;
    }

    public int size() {
        return size.get();
    }
    
    protected int incrementAndGet(){
        return size.incrementAndGet();
    }

    static class Node<V> {
        public Node(V v) {
            this.v = v;
        }

        final V v;
        Node<V> next;
        boolean validate = true;
    }

    //     not sure if this useful
    long p00, p01, p02, p03, p04, p05, p06, p07;
    long p10, p11, p12, p13, p14, p15, p16, p17;

}
