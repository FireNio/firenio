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
package com.firenio.concurrent;

import com.firenio.common.Unsafe;

public class ScmpLinkedQueue<V> extends ScspLinkedQueue<V> {

    private static final long tailOffset;

    static {
        try {
            tailOffset = Unsafe.objectFieldOffset(ScspLinkedQueue.class.getDeclaredField("tail"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

    public void offer(V v) {
        Node<V> n    = new Node<>();
        Node<V> tail = getTail();
        if (Unsafe.compareAndSwapObject(this, tailOffset, tail, n)) {
            tail.v = v;
            tail.next = n;
            incrementAndGet();
            return;
        }
        for (; ; ) {
            tail = getTail();
            if (Unsafe.compareAndSwapObject(this, tailOffset, tail, n)) {
                tail.v = v;
                tail.next = n;
                incrementAndGet();
                return;
            }
        }
    }

}
