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

public class FixedAtomicInteger {

    private AtomicInteger atomiticInteger;

    private int           max_value;

    private int           min_value;

    public FixedAtomicInteger() {
        this(0, Integer.MAX_VALUE);
    }

    public FixedAtomicInteger(int max) {
        this(0, max);
    }

    public FixedAtomicInteger(int min, int max) {
        this.min_value = min;
        this.max_value = max;
        this.atomiticInteger = new AtomicInteger(min);
    }

    public boolean compareAndSet(int expect, int update) {
        return atomiticInteger.compareAndSet(expect, update);
    }

    public final int decrementAndGet() {
        for (;;) {
            int current = atomiticInteger.get();
            int next;
            if (current == min_value) {
                next = max_value;
            } else {
                next = current - 1;
            }
            if (atomiticInteger.compareAndSet(current, next)) {
                return next;
            }
        }
    }

    public final int getAndDecrement() {
        for (;;) {
            int current = atomiticInteger.get();
            int next;
            if (current == min_value) {
                next = max_value;
            } else {
                next = current - 1;
            }
            if (atomiticInteger.compareAndSet(current, next)) {
                return current;
            }
        }
    }

    public final int getAndIncrement() {
        for (;;) {
            int current = atomiticInteger.get();
            int next;
            if (current == max_value) {
                next = min_value;
            } else {
                next = current + 1;
            }
            if (atomiticInteger.compareAndSet(current, next)) {
                return current;
            }
        }
    }

    public int getMaxValue() {
        return max_value;
    }

    public int getMinValue() {
        return min_value;
    }

    public final int incrementAndGet() {
        for (;;) {
            int current = atomiticInteger.get();
            int next;
            if (current == max_value) {
                next = min_value;
            } else {
                next = current + 1;
            }
            if (atomiticInteger.compareAndSet(current, next)) {
                return next;
            }
        }
    }
}
