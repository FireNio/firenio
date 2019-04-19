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

/**
 * @author wangkai
 */
public class IntArray {

    private int[] array;
    private int   size;

    public IntArray() {
        this(16);
    }

    public IntArray(int capacity) {
        array = new int[capacity];
    }

    public void add(int value) {
        checkCapacity();
        array[size++] = value;
    }

    public int capacity() {
        return array.length;
    }

    private void checkCapacity() {
        if (array.length == size) {
            array = Arrays.copyOf(array, array.length * 2);
        }
    }

    public void clear() {
        size = 0;
    }

    public int get(int index) {
        return array[index];
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }
}
