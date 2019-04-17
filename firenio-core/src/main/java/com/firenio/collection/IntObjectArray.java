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
public class IntObjectArray<V> {

    private int[] intArray;
    private V[]   objArray;
    private int   size;

    public IntObjectArray() {
        this(16);
    }

    @SuppressWarnings("unchecked")
    public IntObjectArray(int capacity) {
        intArray = new int[capacity];
        objArray = (V[]) new Object[capacity];
    }

    public void add(int intV, V objV) {
        checkCapacity();
        intArray[size] = intV;
        objArray[size++] = objV;
    }

    public int capacity() {
        return intArray.length;
    }

    private void checkCapacity() {
        if (intArray.length == size) {
            intArray = Arrays.copyOf(intArray, intArray.length * 2);
            objArray = Arrays.copyOf(objArray, objArray.length * 2);
        }
    }

    public void clear() {
        Arrays.fill(objArray, 0, size, null);
        size = 0;
    }

    public int getInt(int index) {
        return intArray[index];
    }

    public V getObject(int index) {
        return objArray[index];
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

}
