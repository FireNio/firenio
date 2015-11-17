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
package com.firenio.baseio.collection;

import java.util.Arrays;

/**
 * @author wangkai
 *
 */
public class ArrayListStack<V> implements Stack<V> {

    private V[] list;
    private int max;
    private int size;

    @SuppressWarnings("unchecked")
    public ArrayListStack(int max) {
        this.max = max;
        this.list = (V[]) new Object[max];
    }

    @Override
    public void clear() {
        Arrays.fill(list, 0, size, null);
        size = 0;
    }

    @Override
    public V pop() {
        if (size == 0) {
            return null;
        }
        return list[--size];
    }

    @Override
    public void push(V v) {
        if (size < max) {
            list[size++] = v;
        }
    }

    @Override
    public int size() {
        return size;
    }

}
