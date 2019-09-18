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

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author: wangkai
 **/
public class ObjectPool<T> {

    public static final ObjectPool BLANK = new BlankObjectPool<>();

    private       int      size;
    private final int      capacity;
    private final T[]      elements;
    private final Thread   ownerThread;
    private final Queue<T> conElements = new ConcurrentLinkedQueue<>();

    ObjectPool() {
        this.ownerThread = null;
        this.elements = null;
        this.capacity = 0;
        this.size = 0;
    }

    public ObjectPool(Thread ownerThread, int capacity) {
        this.capacity = capacity;
        this.ownerThread = ownerThread;
        this.elements = (T[]) new Object[capacity];
    }

    private void initElements(T[] elements, InitValueFunction<T> fun) {
        for (int i = 0; i < elements.length; i++) {
            elements[i] = fun.newItem(this);
        }
    }

    public T pop() {
        if (size > 0) {
            return elements[--size];
        }
        return conElements.poll();
    }

    public void push(T t) {
        Thread currentThread = Thread.currentThread();
        if (currentThread == ownerThread) {
            if (size < capacity) {
                elements[size++] = t;
            }
        } else {
            conElements.offer(t);
        }
    }

    public int capacity() {
        return capacity;
    }

    private interface InitValueFunction<T> {

        T newItem(ObjectPool<T> pool);
    }

    static class BlankObjectPool<T> extends ObjectPool<T> {

        @Override
        public T pop() {
            return null;
        }

        @Override
        public void push(T t) { }

        @Override
        public int capacity() {
            return 0;
        }
    }

}
