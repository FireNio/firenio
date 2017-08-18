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

public class ScspBlockingQueue<E> {

    protected E[]           items;

    protected int           capacity;

    protected int           takeIndex;

    protected int           putIndex;

    protected int           notFullCount;

    protected AtomicInteger count    = new AtomicInteger();

    protected Object        notEmpty = new Object();

    protected Object        notFull  = new Object();

    protected void enqueue(E x) {
        final Object[] items = this.items;
        items[putIndex] = x;
        if (++putIndex == capacity) {
            putIndex = 0;
        }
        int c = count.incrementAndGet();
        if (c == 1) {
            synchronized (notEmpty) {
                notEmpty.notify();
            }
        }
    }

    protected E dequeue() {
        E[] items = this.items;
        E x = items[takeIndex];
        items[takeIndex] = null;
        if (++takeIndex == capacity) {
            takeIndex = 0;
        }
        int c = count.decrementAndGet();
        if (c == notFullCount) {
            synchronized (notFull) {
                notFull.notify();
            }
        }
        return x;
    }

    @SuppressWarnings("unchecked")
    public ScspBlockingQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException();
        }
        items = (E[]) new Object[capacity];
        notFullCount = capacity - 1;
        this.capacity = capacity;
    }

    public boolean offer(E e) {
        if (count.get() == capacity) {
            return false;
        } else {
            enqueue(e);
            return true;
        }
    }

    public void put(E e) throws InterruptedException {
        offer(e, 0);
    }

    public boolean offer(E e, long timeout) throws InterruptedException {
        if (count.get() == capacity) {
            synchronized (notFull) {
                if (count.get() == capacity) {
                    notFull.wait(timeout);
                }
            }
        }
        return offer(e);
    }

    public E poll() {
        if (count.get() == 0) {
            return null;
        }
        return dequeue();
    }

    public E take() throws InterruptedException {
        return poll(0);
    }

    public E poll(long timeout) throws InterruptedException {
        if (count.get() == 0) {
            synchronized (notEmpty) {
                if (count.get() == 0) {
                    notEmpty.wait(timeout);
                }
            }
        }
        return poll();
    }

    public int size() {
        return count.get();
    }

    public int remainingCapacity() {
        return capacity - count.get();
    }

}
