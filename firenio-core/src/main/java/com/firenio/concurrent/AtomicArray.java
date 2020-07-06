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

import java.util.Arrays;


public final class AtomicArray {

    private volatile Object[] array;

    public AtomicArray() {
        this(8);
    }

    public AtomicArray(int cap) {
        this.array = new Object[cap];
    }

    public <T> T get(int index) {
        Object[] array = this.array;
        if (index < array.length) {
            return (T) array[index];
        }
        return null;
    }

    public synchronized void set(int index, Object value) {
        Object[] array = this.array;
        if (index < array.length) {
            array[index] = value;
        } else {
            this.array = Arrays.copyOf(array, index + 1);
            this.array[index] = value;
        }
    }

}
