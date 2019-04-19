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

import java.util.Arrays;

import com.firenio.common.Util;

/**
 * NOT THREAD SAFE
 *
 * @author wangkai
 */
public class DelayedQueue {

    private static final int         INITIAL_CAPACITY = 16;
    private              DelayTask[] queue            = new DelayTask[INITIAL_CAPACITY];
    private              int         size             = 0;

    public void clear() {
        for (int i = 0; i < size; i++) {
            DelayTask t = queue[i];
            if (t != null) {
                queue[i] = null;
                setIndex(t, -1);
            }
        }
        size = 0;
    }

    public boolean contains(Object x) {
        return indexOf(x) != -1;
    }

    private DelayTask finishPoll(DelayTask f) {
        int       s = --size;
        DelayTask x = queue[s];
        queue[s] = null;
        if (s != 0)
            siftDown(0, x);
        setIndex(f, -1);
        return f;
    }

    /**
     * Resizes the heap array.  Call only when holding lock.
     */
    private void grow() {
        int oldCapacity = queue.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1); // grow 50%
        if (newCapacity < 0) // overflow
            newCapacity = Integer.MAX_VALUE;
        queue = Arrays.copyOf(queue, newCapacity);
    }

    /**
     * Finds index of given object, or -1 if absent.
     */
    private int indexOf(Object x) {
        if (x != null) {
            for (int i = 0; i < size; i++)
                if (x.equals(queue[i]))
                    return i;
        }
        return -1;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean offer(DelayTask e) {
        if (e == null)
            throw new NullPointerException();
        int i = size;
        if (i >= queue.length)
            grow();
        size = i + 1;
        if (i == 0) {
            queue[0] = e;
            setIndex(e, 0);
        } else {
            siftUp(i, e);
        }
        return true;
    }

    public DelayTask peek() {
        return queue[0];
    }

    public DelayTask poll() {
        if (isEmpty()) {
            return null;
        }
        return finishPoll(queue[0]);
    }

    public int remainingCapacity() {
        return Integer.MAX_VALUE;
    }

    public boolean remove(Object x) {
        int i = indexOf(x);
        if (i < 0)
            return false;
        setIndex(queue[i], -1);
        int       s           = --size;
        DelayTask replacement = queue[s];
        queue[s] = null;
        if (s != i) {
            siftDown(i, replacement);
            if (queue[i] == replacement)
                siftUp(i, replacement);
        }
        return true;
    }

    private void setIndex(DelayTask f, int idx) {

    }

    /**
     * Sifts element added at top down to its heap-ordered spot.
     * Call only when holding lock.
     */
    private void siftDown(int k, DelayTask key) {
        int half = size >>> 1;
        while (k < half) {
            int       child = (k << 1) + 1;
            DelayTask c     = queue[child];
            int       right = child + 1;
            if (right < size && c.compareTo(queue[right]) > 0)
                c = queue[child = right];
            if (key.compareTo(c) <= 0)
                break;
            queue[k] = c;
            setIndex(c, k);
            k = child;
        }
        queue[k] = key;
        setIndex(key, k);
    }

    /**
     * Sifts element added at bottom up to its heap-ordered spot.
     * Call only when holding lock.
     */
    private void siftUp(int k, DelayTask key) {
        while (k > 0) {
            int       parent = (k - 1) >>> 1;
            DelayTask e      = queue[parent];
            if (key.compareTo(e) >= 0)
                break;
            queue[k] = e;
            setIndex(e, k);
            k = parent;
        }
        queue[k] = key;
        setIndex(key, k);
    }

    public int size() {
        return size;
    }

    /**
     * NOT THREAD SAFE:
     * all method can only invoke in its event loop
     */
    public abstract static class DelayTask implements Runnable, Comparable<DelayTask> {

        static final long CANCEL_MASK = 1L << 63;
        static final long DONE_MASK   = 1L << 62;
        static final long DELAY_MASK  = ~(CANCEL_MASK | DONE_MASK);

        private long flags;

        public DelayTask(long delay) {
            this.flags = delay + Util.now();
        }

        public void cancel() {
            this.flags |= CANCEL_MASK;
        }

        @Override
        public int compareTo(DelayTask o) {
            return (int) (getDelay() - o.getDelay());
        }

        public long getDelay() {
            return (this.flags & DELAY_MASK);
        }

        public boolean isCanceled() {
            return (this.flags & CANCEL_MASK) != 0;
        }

        public boolean isDone() {
            return (this.flags & DONE_MASK) != 0;
        }

        public void done() {
            this.flags |= DONE_MASK;
        }

    }

}
